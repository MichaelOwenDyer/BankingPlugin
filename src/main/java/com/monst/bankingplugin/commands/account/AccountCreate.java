package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.banking.bank.BankField;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountCreateEvent;
import com.monst.bankingplugin.events.account.AccountPreCreateEvent;
import com.monst.bankingplugin.exceptions.BankNotFoundException;
import com.monst.bankingplugin.exceptions.ChestBlockedException;
import com.monst.bankingplugin.geo.locations.ChestLocation;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AccountCreate extends AccountCommand.SubCommand {

    AccountCreate() {
        super("create", true);
    }

    @Override
    protected String getPermission() {
        return Permissions.ACCOUNT_CREATE;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_CREATE;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        plugin.debug(p.getName() + " wants to create an account");

        if (!hasPermission(p, Permissions.ACCOUNT_CREATE)) {
            p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_CREATE));
            plugin.debug(p.getName() + " is not permitted to create an account");
            return true;
        }

        int limit = Utils.getAccountLimit(p);
        if (limit != -1 && accountRepo.getOwnedBy(p).size() >= limit) {
            p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_LIMIT_REACHED, new Replacement(Placeholder.LIMIT, limit)));
            plugin.debug(p.getName() + " has reached their account limit");
            return true;
        }

        AccountPreCreateEvent event = new AccountPreCreateEvent(p, args);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Account pre-create event cancelled");
            return true;
        }

        plugin.debug(p.getName() + " can now click a chest to create an account");
        p.sendMessage(LangUtils.getMessage(Message.CLICK_CHEST_CREATE));
        ClickType.setPlayerClickType(p, ClickType.create());
        return true;
    }

    /**
     * Create a new account
     *
     * @param p  Player who executed the command will receive the message
     *                          and become the owner of the account
     * @param c  Chest where the account will be located
     */
    public static void create(Player p, Chest c) {
        ChestLocation chestLocation = ChestLocation.from(c);

        if (plugin.getAccountRepository().isAccount(chestLocation)) {
            p.sendMessage(LangUtils.getMessage(Message.CHEST_ALREADY_ACCOUNT));
            plugin.debug("Chest is already an account.");
            return;
        }

        try {
            chestLocation.checkSpaceAbove();
        } catch (ChestBlockedException e) {
            p.sendMessage(LangUtils.getMessage(Message.CHEST_BLOCKED));
            plugin.debug("Chest is blocked.");
            return;
        }

        Bank bank;
        try {
            bank = chestLocation.getBank();
        } catch (BankNotFoundException e) {
            p.sendMessage(LangUtils.getMessage(Message.CHEST_NOT_IN_BANK));
            plugin.debug("Chest is not in a bank.");
            return;
        }

        if (!Config.allowSelfBanking && bank.isOwner(p)) {
            p.sendMessage(LangUtils.getMessage(Message.NO_SELF_BANKING, new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)));
            plugin.debug(p.getName() + " is not permitted to create an account at their own bank");
            return;
        }
        int playerAccountLimit = bank.get(BankField.PLAYER_BANK_ACCOUNT_LIMIT);
        if (playerAccountLimit > 0 && bank.getAccounts(account -> account.isOwner(p)).size() >= playerAccountLimit) {
            p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_LIMIT_AT_BANK_REACHED,
                    new Replacement(Placeholder.BANK_NAME, bank::getColorizedName),
                    new Replacement(Placeholder.LIMIT, playerAccountLimit)
            ));
            plugin.debug(p.getName() + " is not permitted to create another account at bank " + bank.getName());
            return;
        }

        Account account = Account.mint(p, chestLocation);

        AccountCreateEvent event = new AccountCreateEvent(p, account);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_CREATE_PROTECTED)) {
            plugin.debug("No permission to create account on a protected chest.");
            p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_CREATE_PROTECTED));
            return;
        }

        double creationPrice = bank.get(BankField.ACCOUNT_CREATION_PRICE);
        creationPrice *= chestLocation.getSize();
        creationPrice *= bank.isOwner(p) ? 0 : 1;

        double balance = plugin.getEconomy().getBalance(p);
        if (creationPrice > 0 && creationPrice > balance) {
            p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_CREATE_INSUFFICIENT_FUNDS,
                    new Replacement(Placeholder.PRICE, creationPrice),
                    new Replacement(Placeholder.PLAYER_BALANCE, balance),
                    new Replacement(Placeholder.AMOUNT_REMAINING, creationPrice - balance)
            ));
            return;
        }

        double finalCreationPrice = creationPrice;
        if (creationPrice > 0)
        // Account owner pays the bank owner the creation fee
        if (!Utils.withdrawPlayer(p, creationPrice, Callback.of(plugin,
                result -> p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_CREATE_FEE_PAID,
                        new Replacement(Placeholder.PRICE, finalCreationPrice),
                        new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)
                )),
                error -> p.sendMessage(LangUtils.getMessage(Message.ERROR_OCCURRED, new Replacement(Placeholder.ERROR, error::getLocalizedMessage)))
        )))
            return;

        // Bank owner receives the payment from the customer
        if (creationPrice > 0 && bank.isPlayerBank()) {
            OfflinePlayer bankOwner = account.getBank().getOwner();
            Utils.depositPlayer(bankOwner, creationPrice, Callback.of(plugin,
                    result -> Messenger.notify(bankOwner, LangUtils.getMessage(Message.ACCOUNT_CREATE_FEE_RECEIVED,
                            new Replacement(Placeholder.PLAYER, p::getName),
                            new Replacement(Placeholder.AMOUNT, finalCreationPrice),
                            new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)
                    )),
                    error -> Messenger.notify(bankOwner, LangUtils.getMessage(Message.ERROR_OCCURRED, new Replacement(Placeholder.ERROR, error::getLocalizedMessage)))
            ));
        }

        if (account.create(true)) {
            plugin.debug("Account created");
            accountRepo.add(account, true, account.callUpdateName());
            p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_CREATED, new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)));
        } else {
            plugin.debugf("Could not create account");
            p.sendMessage(LangUtils.getMessage(Message.ERROR_OCCURRED, new Replacement(Placeholder.ERROR, "Could not create account")));
        }
    }
}
