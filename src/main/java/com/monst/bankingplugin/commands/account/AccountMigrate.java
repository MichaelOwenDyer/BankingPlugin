package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.banking.AccountField;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.events.account.AccountMigrateCommandEvent;
import com.monst.bankingplugin.events.account.AccountMigrateEvent;
import com.monst.bankingplugin.geo.locations.AccountLocation;
import com.monst.bankingplugin.lang.*;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.PayrollOffice;
import com.monst.bankingplugin.utils.Permissions;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class AccountMigrate extends SubCommand.AccountSubCommand {

    AccountMigrate() {
        super("migrate", true);
    }

    @Override
    protected String getPermission() {
        return Permissions.ACCOUNT_CREATE;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_MIGRATE;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        PLUGIN.debug(p.getName() + " wants to migrate an account");

        if (!p.hasPermission(Permissions.ACCOUNT_MIGRATE)) {
            PLUGIN.debug(p.getName() + " does not have permission to migrate an account");
            p.sendMessage(Messages.get(Message.NO_PERMISSION_ACCOUNT_MIGRATE));
            return true;
        }

        AccountMigrateCommandEvent event = new AccountMigrateCommandEvent(p, args);
        event.fire();
        if (event.isCancelled()) {
            PLUGIN.debug("Account migrate command event cancelled");
            return true;
        }

        p.sendMessage(Messages.get(Message.CLICK_ACCOUNT_MIGRATE));
        ClickType.setMigrateClickType(p);
        PLUGIN.debug(p.getName() + " is migrating an account");
        return true;
    }

    public static void selectAccount(Player p, Account accountToMove) {

        if (!accountToMove.isOwner(p) && !p.hasPermission(Permissions.ACCOUNT_MIGRATE_OTHER)) {
            if (accountToMove.isTrusted(p)) {
                PLUGIN.debugf("%s cannot migrate account #%d as a co-owner", p.getName(), accountToMove.getID());
                p.sendMessage(Messages.get(Message.MUST_BE_OWNER));
                return;
            }
            PLUGIN.debugf("%s does not have permission to migrate account #%d", p.getName(), accountToMove.getID());
            p.sendMessage(Messages.get(Message.NO_PERMISSION_ACCOUNT_MIGRATE_OTHER));
            return;
        }

        PLUGIN.debugf("%s wants to migrate account #%d", p.getName(), accountToMove.getID());
        ClickType.setMigrateClickType(p, accountToMove);
        p.sendMessage(Messages.get(Message.CLICK_CHEST_MIGRATE));

    }

    public static void selectNewChest(Player p, Account accountToMove, Block targetBlock) {
        Account clickedAccount = accountRepo.getAt(targetBlock);
        if (clickedAccount != null) {
            if (Objects.equals(accountToMove, clickedAccount)) {
                PLUGIN.debugf("%s clicked the same chest to migrate to.", p.getName());
                p.sendMessage(Messages.get(Message.SAME_CHEST));
            } else {
                PLUGIN.debugf("%s clicked an already existing account chest to migrate to", p.getName());
                p.sendMessage(Messages.get(Message.CHEST_ALREADY_ACCOUNT));
            }
            return;
        }

        Chest c = (Chest) targetBlock.getState();
        AccountLocation newAccountLocation = AccountLocation.from(c.getInventory().getHolder());

        if (newAccountLocation.isBlocked()) {
            p.sendMessage(Messages.get(Message.CHEST_BLOCKED));
            PLUGIN.debug("Chest is blocked.");
            return;
        }

        Bank newBank = newAccountLocation.getBank();
        if (newBank == null) {
            p.sendMessage(Messages.get(Message.CHEST_NOT_IN_BANK));
            PLUGIN.debug("Chest is not in a bank.");
            return;
        }

        if (!Objects.equals(accountToMove.getBank(), newBank) && !p.hasPermission(Permissions.ACCOUNT_MIGRATE_BANK)) {
            p.sendMessage(Messages.get(Message.NO_PERMISSION_ACCOUNT_MIGRATE_BANK));
            PLUGIN.debugf("%s does not have permission to migrate their account to another bank.", p.getName());
            return;
        }

        AccountMigrateEvent event = new AccountMigrateEvent(p, accountToMove, newAccountLocation);
        event.fire();
        if (event.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
            PLUGIN.debug("No permission to create account on a protected chest.");
            p.sendMessage(Messages.get(Message.NO_PERMISSION_ACCOUNT_CREATE_PROTECTED));
            return;
        }

        Bank oldBank = accountToMove.getBank();

        double creationPrice = newBank.getAccountCreationPrice().get();
        creationPrice *= (newBank.isOwner(p) ? 0 : 1);
        creationPrice *= newAccountLocation.getSize();

        double reimbursement = oldBank.getReimburseAccountCreation().get() ?
                oldBank.getAccountCreationPrice().get() :
                0.0d;
        reimbursement *= (oldBank.isOwner(p) ? 0 : 1); // Free if owner
        reimbursement *= accountToMove.getSize(); // Double chest is worth twice as much

        double net = reimbursement - creationPrice;
        if (!PayrollOffice.allowPayment(p, net)) {
            double balance = PLUGIN.getEconomy().getBalance(p);
            p.sendMessage(Messages.get(Message.ACCOUNT_CREATE_INSUFFICIENT_FUNDS,
                    new Replacement(Placeholder.PRICE, net),
                    new Replacement(Placeholder.PLAYER_BALANCE, balance),
                    new Replacement(Placeholder.AMOUNT_REMAINING, net - balance)
            ));
            return;
        }

        if (reimbursement > 0) {
            // Customer receives reimbursement for old account
            if (PayrollOffice.deposit(p, reimbursement))
                p.sendMessage(Messages.get(Message.REIMBURSEMENT_RECEIVED,
                        new Replacement(Placeholder.AMOUNT, reimbursement)
                ));
            // Bank owner of old account pays reimbursement
            if (oldBank.isPlayerBank() && PayrollOffice.withdraw(oldBank.getOwner(), reimbursement))
                Mailman.notify(oldBank.getOwner(), Messages.get(Message.REIMBURSEMENT_PAID,
                        new Replacement(Placeholder.PLAYER, p::getName),
                        new Replacement(Placeholder.AMOUNT, reimbursement)
                ));
        }

        if (creationPrice > 0) {
            // Account owner pays creation fee for new account
            if (PayrollOffice.withdraw(p, creationPrice))
                p.sendMessage(Messages.get(Message.ACCOUNT_CREATE_FEE_PAID,
                        new Replacement(Placeholder.PRICE, creationPrice),
                        new Replacement(Placeholder.BANK_NAME, newBank::getColorizedName)
                ));
            else
                return;
            // Bank owner of new account receives account creation fee
            if (newBank.isPlayerBank() && PayrollOffice.deposit(newBank.getOwner(), creationPrice))
                Mailman.notify(newBank.getOwner(), Messages.get(Message.ACCOUNT_CREATE_FEE_RECEIVED,
                        new Replacement(Placeholder.PLAYER, p::getName),
                        new Replacement(Placeholder.AMOUNT, creationPrice),
                        new Replacement(Placeholder.BANK_NAME, newBank::getColorizedName)
                ));
        }

        accountToMove.clearChestName();
        accountToMove.setLocation(newAccountLocation);
        accountToMove.setBank(newBank);
        accountRepo.update(accountToMove, accountToMove.callUpdateChestName(), AccountField.BANK, AccountField.LOCATION);
        p.sendMessage(Messages.get(Message.ACCOUNT_MIGRATED));
        PLUGIN.debugf("Account migrated (#%d)", accountToMove.getID());
    }

}
