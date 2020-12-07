package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.banking.bank.BankField;
import com.monst.bankingplugin.events.account.AccountMigrateEvent;
import com.monst.bankingplugin.events.account.AccountPreMigrateEvent;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
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

        AccountPreMigrateEvent event = new AccountPreMigrateEvent(p, toMigrate);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Account pre-migrate event cancelled");
            return;
        }

        plugin.debugf("%s wants to migrate account #%d", p.getName(), toMigrate.getID());
        ClickType.setPlayerClickType(p, ClickType.migrate(toMigrate));
        p.sendMessage(LangUtils.getMessage(Message.CLICK_CHEST_MIGRATE));

    }

    public static void migratePartTwo(Player p, Block b, Account toMigrate) {
        AccountUtils accountUtils = plugin.getAccountUtils();
        Location newLocation = b.getLocation();
        if (accountUtils.isAccount(newLocation)) {
            if (toMigrate.equals(accountUtils.getAccount(newLocation))) {
                plugin.debugf("%s clicked the same chest to migrate to.", p.getName());
                p.sendMessage(LangUtils.getMessage(Message.SAME_CHEST));
                return;
            }
            plugin.debugf("%s clicked an already existing account chest to migrate to", p.getName());
            p.sendMessage(LangUtils.getMessage(Message.CHEST_ALREADY_ACCOUNT));
            return;
        }
        if (!Utils.isTransparent(b.getRelative(BlockFace.UP))) {
            p.sendMessage(LangUtils.getMessage(Message.CHEST_BLOCKED));
            plugin.debug("Chest is blocked.");
            return;
        }
        Bank newBank = plugin.getBankUtils().getBank(newLocation); // May or may not be the same as previous bank
        if (newBank == null) {
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
        newAccount.setLocation(newLocation);

        AccountMigrateEvent event = new AccountMigrateEvent(p, newAccount, newLocation);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
            plugin.debug("No permission to create account on a protected chest.");
            p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_CREATE_PROTECTED));
            return;
        }

        Bank oldBank = toMigrate.getBank();

        double creationPrice = newBank.get(BankField.ACCOUNT_CREATION_PRICE);
        creationPrice *= (((Chest) b.getState()).getInventory().getHolder() instanceof DoubleChest ? 2 : 1);
        creationPrice *= (newBank.isOwner(p) ? 0 : 1);

        double reimbursement = oldBank.get(BankField.REIMBURSE_ACCOUNT_CREATION) ?
                oldBank.get(BankField.ACCOUNT_CREATION_PRICE) :
                0.0d;
        reimbursement *= toMigrate.getSize(); // Double chest is worth twice as much
        reimbursement *= (oldBank.isOwner(p) ? 0 : 1); // Free if owner

        double net = reimbursement - creationPrice;
        double balance = plugin.getEconomy().getBalance(p);
        if (net < 0 && balance < net * -1) { // TODO: Test
            p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_CREATE_INSUFFICIENT_FUNDS,
                    new Replacement(Placeholder.PRICE, net),
                    new Replacement(Placeholder.PLAYER_BALANCE, balance),
                    new Replacement(Placeholder.AMOUNT_REMAINING, net - balance)
            ));
            return;
        }

        final double finalReimbursement = reimbursement;
        final double finalCreationPrice = creationPrice;

        // Customer receives reimbursement for old account
        if (finalReimbursement > 0 && !oldBank.isOwner(p)) {
            Utils.depositPlayer(p.getPlayer(), finalReimbursement, Callback.of(plugin,
                    result -> p.sendMessage(LangUtils.getMessage(Message.REIMBURSEMENT_RECEIVED,
                            new Replacement(Placeholder.AMOUNT, finalReimbursement)
                    )),
                    error -> p.sendMessage(LangUtils.getMessage(Message.ERROR_OCCURRED,
                            new Replacement(Placeholder.ERROR, error::getLocalizedMessage)
                    ))
            ));
        }

        // Bank owner of new account receives account creation fee
        if (finalCreationPrice > 0 && newBank.isPlayerBank() && !newBank.isOwner(p)) {
            OfflinePlayer bankOwner = newBank.getOwner();
            Utils.depositPlayer(bankOwner, finalCreationPrice, Callback.of(plugin,
                    result -> Messenger.notify(bankOwner, LangUtils.getMessage(Message.ACCOUNT_CREATE_FEE_RECEIVED,
                            new Replacement(Placeholder.PLAYER, p::getName),
                            new Replacement(Placeholder.AMOUNT, finalCreationPrice),
                            new Replacement(Placeholder.BANK_NAME, newBank::getColorizedName)
                    )),
                    error -> Messenger.notify(bankOwner, LangUtils.getMessage(Message.ERROR_OCCURRED,
                            new Replacement(Placeholder.ERROR, error::getLocalizedMessage)
                    ))
            ));
        }

        // Account owner pays creation fee for new account
        if (creationPrice > 0 && !newBank.isOwner(p)) {
            if (!Utils.withdrawPlayer(p, finalCreationPrice, Callback.of(plugin,
                    result -> p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_CREATE_FEE_PAID,
                            new Replacement(Placeholder.PRICE, finalCreationPrice),
                            new Replacement(Placeholder.BANK_NAME, newBank::getColorizedName)
                    )),
                    error -> p.sendMessage(LangUtils.getMessage(Message.ERROR_OCCURRED,
                            new Replacement(Placeholder.ERROR, error::getLocalizedMessage)
                    ))
            )))
                return;
        }

        // Bank owner of old account pays reimbursement
        if (reimbursement > 0 && oldBank.isPlayerBank() && !oldBank.isOwner(p)) {
            OfflinePlayer bankOwner = oldBank.getOwner();
            Utils.withdrawPlayer(bankOwner, finalReimbursement, Callback.of(plugin,
                    result -> Messenger.notify(bankOwner, LangUtils.getMessage(Message.REIMBURSEMENT_PAID,
                            new Replacement(Placeholder.PLAYER, p::getName),
                            new Replacement(Placeholder.AMOUNT, finalReimbursement)
                    )),
                    error -> Messenger.notify(bankOwner, LangUtils.getMessage(Message.ERROR_OCCURRED,
                            new Replacement(Placeholder.ERROR, error::getLocalizedMessage)
                    ))
            ));
        }

        if (newAccount.create(true)) {
            plugin.debugf("Account migrated (#%d)", newAccount.getID());
            accountUtils.removeAccount(toMigrate, false, Callback.of(plugin,
                    result -> {
                        accountUtils.addAccount(newAccount, true, newAccount.callUpdateName()); // Database entry is replaced
                        p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_MIGRATED));
                    },
                    error -> p.sendMessage(LangUtils.getMessage(Message.ERROR_OCCURRED,
                            new Replacement(Placeholder.ERROR, error::getLocalizedMessage)
                    ))
            ));
        }
    }

}
