package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.account.AccountField;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.events.account.AccountMigrateCommandEvent;
import com.monst.bankingplugin.events.account.AccountMigrateEvent;
import com.monst.bankingplugin.exceptions.AccountNotFoundException;
import com.monst.bankingplugin.exceptions.BankNotFoundException;
import com.monst.bankingplugin.exceptions.ChestBlockedException;
import com.monst.bankingplugin.geo.locations.ChestLocation;
import com.monst.bankingplugin.lang.*;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.PayrollOffice;
import com.monst.bankingplugin.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AccountMigrate extends AccountCommand.SubCommand {

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
        plugin.debug(p.getName() + " wants to migrate an account");

        if (!p.hasPermission(Permissions.ACCOUNT_MIGRATE)) {
            plugin.debug(p.getName() + " does not have permission to migrate an account");
            p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_MIGRATE));
            return true;
        }

        p.sendMessage(LangUtils.getMessage(Message.CLICK_ACCOUNT_MIGRATE));
        ClickType.setPlayerClickType(p, ClickType.migrate(null));
        plugin.debug(p.getName() + " is migrating an account");
        return true;
    }

    public static void migratePartOne(Player p, Account toMigrate) {

        if (!toMigrate.isOwner(p) && !p.hasPermission(Permissions.ACCOUNT_MIGRATE_OTHER)) {
            if (toMigrate.isTrusted(p)) {
                plugin.debugf("%s cannot migrate account #%d as a co-owner", p.getName(), toMigrate.getID());
                p.sendMessage(LangUtils.getMessage(Message.MUST_BE_OWNER));
                return;
            }
            plugin.debugf("%s does not have permission to migrate account #%d", p.getName(), toMigrate.getID());
            p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_MIGRATE_OTHER));
            return;
        }

        AccountMigrateCommandEvent event = new AccountMigrateCommandEvent(p, toMigrate);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Account pre-migrate event cancelled");
            return;
        }

        plugin.debugf("%s wants to migrate account #%d", p.getName(), toMigrate.getID());
        ClickType.setPlayerClickType(p, ClickType.migrate(toMigrate));
        p.sendMessage(LangUtils.getMessage(Message.CLICK_CHEST_MIGRATE));

    }

    public static void migratePartTwo(Player p, Chest c, Account toMigrate) {
        ChestLocation chestLocation = ChestLocation.from(c);
        try {
            if (toMigrate.equals(accountRepo.getAt(chestLocation))) {
                plugin.debugf("%s clicked the same chest to migrate to.", p.getName());
                p.sendMessage(LangUtils.getMessage(Message.SAME_CHEST));
            } else {
                plugin.debugf("%s clicked an already existing account chest to migrate to", p.getName());
                p.sendMessage(LangUtils.getMessage(Message.CHEST_ALREADY_ACCOUNT));
            }
            return;
        } catch (AccountNotFoundException ignored) {}

        try {
            chestLocation.checkSpaceAbove();
        } catch (ChestBlockedException e) {
            p.sendMessage(LangUtils.getMessage(Message.CHEST_BLOCKED));
            plugin.debug("Chest is blocked.");
            return;
        }

        Bank newBank;
        try {
            newBank = chestLocation.getBank();
        } catch (BankNotFoundException e) {
            p.sendMessage(LangUtils.getMessage(Message.CHEST_NOT_IN_BANK));
            plugin.debug("Chest is not in a bank.");
            return;
        }

        if (!toMigrate.getBank().equals(newBank) && !p.hasPermission(Permissions.ACCOUNT_MIGRATE_BANK)) {
            p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_MIGRATE_BANK));
            plugin.debugf("%s does not have permission to migrate their account to another bank.", p.getName());
            return;
        }

        Account newAccount = Account.clone(toMigrate);
        newAccount.setBank(newBank);
        newAccount.setChestLocation(chestLocation);

        AccountMigrateEvent event = new AccountMigrateEvent(p, newAccount, chestLocation);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
            plugin.debug("No permission to create account on a protected chest.");
            p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_CREATE_PROTECTED));
            return;
        }

        Bank oldBank = toMigrate.getBank();

        double creationPrice = newBank.getAccountCreationPrice().get();
        creationPrice *= (newBank.isOwner(p) ? 0 : 1);
        creationPrice *= chestLocation.getSize();

        double reimbursement = oldBank.getReimburseAccountCreation().get() ?
                oldBank.getAccountCreationPrice().get() :
                0.0d;
        reimbursement *= (oldBank.isOwner(p) ? 0 : 1); // Free if owner
        reimbursement *= toMigrate.getSize(); // Double chest is worth twice as much

        double net = reimbursement - creationPrice;
        if (!PayrollOffice.allowPayment(p, net)) {
            double balance = plugin.getEconomy().getBalance(p);
            p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_CREATE_INSUFFICIENT_FUNDS,
                    new Replacement(Placeholder.PRICE, net),
                    new Replacement(Placeholder.PLAYER_BALANCE, balance),
                    new Replacement(Placeholder.AMOUNT_REMAINING, net - balance)
            ));
            return;
        }

        if (reimbursement > 0) {
            // Customer receives reimbursement for old account
            if (PayrollOffice.deposit(p, reimbursement))
                p.sendMessage(LangUtils.getMessage(Message.REIMBURSEMENT_RECEIVED,
                        new Replacement(Placeholder.AMOUNT, reimbursement)
                ));
            // Bank owner of old account pays reimbursement
            if (oldBank.isPlayerBank() && PayrollOffice.withdraw(oldBank.getOwner(), reimbursement))
                Mailman.notify(oldBank.getOwner(), LangUtils.getMessage(Message.REIMBURSEMENT_PAID,
                        new Replacement(Placeholder.PLAYER, p::getName),
                        new Replacement(Placeholder.AMOUNT, reimbursement)
                ));
        }

        if (creationPrice > 0) {
            // Account owner pays creation fee for new account
            if (PayrollOffice.withdraw(p, creationPrice))
                p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_CREATE_FEE_PAID,
                        new Replacement(Placeholder.PRICE, creationPrice),
                        new Replacement(Placeholder.BANK_NAME, newBank::getColorizedName)
                ));
            else
                return;
            // Bank owner of new account receives account creation fee
            if (newBank.isPlayerBank() && PayrollOffice.deposit(newBank.getOwner(), creationPrice))
                Mailman.notify(newBank.getOwner(), LangUtils.getMessage(Message.ACCOUNT_CREATE_FEE_RECEIVED,
                        new Replacement(Placeholder.PLAYER, p::getName),
                        new Replacement(Placeholder.AMOUNT, creationPrice),
                        new Replacement(Placeholder.BANK_NAME, newBank::getColorizedName)
                ));
        }

        if (newAccount.create(true)) {
            plugin.debugf("Account migrated (#%d)", newAccount.getID());
            accountRepo.remove(toMigrate, false, Callback.of(
                    result -> {
                        accountRepo.update(newAccount, newAccount.callUpdateChestName(), AccountField.BANK, AccountField.LOCATION); // Database entry is replaced
                        p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_MIGRATED));
                    },
                    error -> p.sendMessage(LangUtils.getMessage(Message.ERROR_OCCURRED,
                            new Replacement(Placeholder.ERROR, error::getLocalizedMessage)
                    ))
            ));
        }
    }

}
