package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.banking.AccountField;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.events.account.AccountMigrateCommandEvent;
import com.monst.bankingplugin.events.account.AccountMigrateEvent;
import com.monst.bankingplugin.geo.locations.AccountLocation;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Objects;

public class AccountMigrate extends SubCommand.AccountSubCommand {

    AccountMigrate(BankingPlugin plugin) {
		super(plugin, "migrate", true);
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
        plugin.debug(p.getName() + " wants to migrate an account");

        if (!p.hasPermission(Permissions.ACCOUNT_MIGRATE)) {
            plugin.debug(p.getName() + " does not have permission to migrate an account");
            p.sendMessage(Message.NO_PERMISSION_ACCOUNT_MIGRATE.translate());
            return true;
        }

        AccountMigrateCommandEvent event = new AccountMigrateCommandEvent(p, args);
        event.fire();
        if (event.isCancelled()) {
            plugin.debug("Account migrate command event cancelled");
            return true;
        }

        p.sendMessage(Message.CLICK_ACCOUNT_MIGRATE.translate());
        ClickType.setMigrateClickType(p);
        plugin.debug(p.getName() + " is migrating an account");
        return true;
    }

    public static void selectAccount(BankingPlugin plugin, Player p, Account accountToMove) {
        if (!accountToMove.isOwner(p) && !p.hasPermission(Permissions.ACCOUNT_MIGRATE_OTHER)) {
            if (accountToMove.isTrusted(p)) {
                plugin.debugf("%s cannot migrate account #%d as a co-owner", p.getName(), accountToMove.getID());
                p.sendMessage(Message.MUST_BE_OWNER.translate());
                return;
            }
            plugin.debugf("%s does not have permission to migrate account #%d", p.getName(), accountToMove.getID());
            p.sendMessage(Message.NO_PERMISSION_ACCOUNT_MIGRATE_OTHER.translate());
            return;
        }

        plugin.debugf("%s wants to migrate account #%d", p.getName(), accountToMove.getID());
        ClickType.setMigrateClickType(p, accountToMove);
        p.sendMessage(Message.CLICK_CHEST_MIGRATE.translate());
    }

    public static void selectNewChest(BankingPlugin plugin, Player p, Account accountToMove, Block targetBlock) {
        Account clickedAccount = plugin.getAccountRepository().getAt(targetBlock);
        if (clickedAccount != null) {
            if (Objects.equals(accountToMove, clickedAccount)) {
                plugin.debugf("%s clicked the same chest to migrate to.", p.getName());
                p.sendMessage(Message.SAME_CHEST.translate());
            } else {
                plugin.debugf("%s clicked an already existing account chest to migrate to", p.getName());
                p.sendMessage(Message.CHEST_ALREADY_ACCOUNT.translate());
            }
            return;
        }

        Chest c = (Chest) targetBlock.getState();
        AccountLocation newAccountLocation = AccountLocation.from(c.getInventory().getHolder());

        if (newAccountLocation.isBlocked()) {
            p.sendMessage(Message.CHEST_BLOCKED.translate());
            plugin.debug("Chest is blocked.");
            return;
        }

        Bank newBank = newAccountLocation.getBank();
        if (newBank == null) {
            p.sendMessage(Message.CHEST_NOT_IN_BANK.translate());
            plugin.debug("Chest is not in a bank.");
            return;
        }

        if (!Objects.equals(accountToMove.getBank(), newBank) && !p.hasPermission(Permissions.ACCOUNT_MIGRATE_BANK)) {
            p.sendMessage(Message.NO_PERMISSION_ACCOUNT_MIGRATE_BANK.translate());
            plugin.debugf("%s does not have permission to migrate their account to another bank.", p.getName());
            return;
        }

        AccountMigrateEvent event = new AccountMigrateEvent(p, accountToMove, newAccountLocation);
        event.fire();
        if (event.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
            plugin.debug("No permission to create account on a protected chest.");
            p.sendMessage(Message.NO_PERMISSION_ACCOUNT_CREATE_PROTECTED.translate());
            return;
        }

        Bank oldBank = accountToMove.getBank();

        BigDecimal creationPrice;
        if (newBank.isOwner(p))
            creationPrice = BigDecimal.ZERO;
        else
            creationPrice = newBank.accountCreationPrice().get().multiply(BigDecimal.valueOf(newAccountLocation.getSize()));

        BigDecimal reimbursement;
        if (oldBank.isOwner(p) || !oldBank.reimburseAccountCreation().get())
            reimbursement = BigDecimal.ZERO;
        else
            reimbursement = oldBank.accountCreationPrice().get().multiply(BigDecimal.valueOf(accountToMove.getSize()));

        BigDecimal net = reimbursement.subtract(creationPrice);
        if (!PayrollOffice.allowPayment(p, net)) {
            BigDecimal balance = BigDecimal.valueOf(plugin.getEconomy().getBalance(p));
            p.sendMessage(Message.ACCOUNT_CREATE_INSUFFICIENT_FUNDS
                    .with(Placeholder.PRICE).as(net)
                    .and(Placeholder.PLAYER_BALANCE).as(balance)
                    .and(Placeholder.AMOUNT_REMAINING).as(net.subtract(balance))
                    .translate());
            return;
        }

        if (reimbursement.signum() > 0) {
            // Customer receives reimbursement for old account
            if (PayrollOffice.deposit(p, reimbursement))
                p.sendMessage(Message.REIMBURSEMENT_RECEIVED.with(Placeholder.AMOUNT).as(reimbursement).translate());
            // Bank owner of old account pays reimbursement
            if (oldBank.isPlayerBank() && PayrollOffice.withdraw(oldBank.getOwner(), reimbursement))
                Utils.notify(oldBank.getOwner(), Message.REIMBURSEMENT_PAID
                        .with(Placeholder.PLAYER).as(p.getName())
                        .and(Placeholder.AMOUNT).as(reimbursement)
                        .translate());
        }

        if (creationPrice.signum() > 0) {
            // Account owner pays creation fee for new account
            if (PayrollOffice.withdraw(p, creationPrice))
                p.sendMessage(Message.ACCOUNT_CREATE_FEE_PAID
                        .with(Placeholder.PRICE).as(creationPrice)
                        .and(Placeholder.BANK_NAME).as(newBank.getColorizedName())
                        .translate());
            else
                return;
            // Bank owner of new account receives account creation fee
            if (newBank.isPlayerBank() && PayrollOffice.deposit(newBank.getOwner(), creationPrice))
                Utils.notify(newBank.getOwner(), Message.ACCOUNT_CREATE_FEE_RECEIVED
                        .with(Placeholder.PLAYER).as(p.getName())
                        .and(Placeholder.AMOUNT).as(creationPrice)
                        .and(Placeholder.BANK_NAME).as(newBank.getColorizedName())
                        .translate());
        }

        accountToMove.clearChestName();
        accountToMove.setLocation(newAccountLocation);
        accountToMove.setBank(newBank);
        plugin.getAccountRepository().update(accountToMove, accountToMove.callUpdateChestName(), AccountField.BANK, AccountField.LOCATION);
        p.sendMessage(Message.ACCOUNT_MIGRATED.translate());
        plugin.debugf("Account migrated (#%d)", accountToMove.getID());
    }

}
