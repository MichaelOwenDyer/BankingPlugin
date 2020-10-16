package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.banking.bank.BankField;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountPreRemoveEvent;
import com.monst.bankingplugin.events.account.AccountRemoveEvent;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AccountRemove extends AccountCommand.SubCommand implements ConfirmableAccountAction {

    private static AccountRemove instance;

    public static AccountRemove getInstance() {
        return instance;
    }

    AccountRemove() {
        super("remove", true);
        instance = this;
    }

    @Override
    protected String getHelpMessage(CommandSender sender) {
        return hasPermission(sender, Permissions.ACCOUNT_CREATE) ? LangUtils.getMessage(Message.COMMAND_USAGE_ACCOUNT_REMOVE, getReplacement()) : "";
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " wants to remove an account");

        AccountPreRemoveEvent event = new AccountPreRemoveEvent(((Player) sender));
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Account pre-remove event cancelled");
            return true;
        }

        plugin.debug(sender.getName() + " can now click a chest to remove an account");
        sender.sendMessage(LangUtils.getMessage(Message.CLICK_ACCOUNT_REMOVE));
        ClickType.setPlayerClickType(((Player) sender), ClickType.remove());
        return true;
    }

    /**
     * Remove an account
     *
     * @param p Player who executed the command and will receive the message
     * @param account  Account to be removed
     */
    public void remove(Player p, Account account) {

        if (!account.isOwner(p) && !p.hasPermission(Permissions.ACCOUNT_REMOVE_OTHER) && !account.getBank().isTrusted(p)) {
            if (account.isTrusted(p))
                p.sendMessage(LangUtils.getMessage(Message.MUST_BE_OWNER));
            else
                p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_REMOVE_OTHER));
            if (!hasEntry(p))
                ClickType.removePlayerClickType(p);
            return;
        }

        if (account.getBalance().signum() > 0 || Config.confirmOnRemove) {
            if (!isConfirmed(p, account.getID())) {
                plugin.debug("Needs confirmation");
                if (account.getBalance().signum() > 0) {
                    p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_BALANCE_NOT_ZERO,
                            new Replacement(Placeholder.ACCOUNT_BALANCE, account::getBalance)
                    ));
                }
                p.sendMessage(LangUtils.getMessage(Message.CLICK_AGAIN_TO_CONFIRM));
                return;
            }
        }

        plugin.debugf("%s is removing %s account (#%d)",
                p.getName(),
                account.isOwner(p) ? "their" : account.getOwner().getName() + "'s",
                account.getID());

        AccountRemoveEvent event = new AccountRemoveEvent(p, account);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Remove event cancelled (#" + account.getID() + ")");
            return;
        }

        Bank bank = account.getBank();
        double creationPrice = bank.get(BankField.ACCOUNT_CREATION_PRICE);
        creationPrice *= account.getSize();
        creationPrice *= bank.get(BankField.REIMBURSE_ACCOUNT_CREATION) ? 1 : 0;

        if (creationPrice > 0 && account.isOwner(p) && !bank.isOwner(p)) {

            double finalCreationPrice = creationPrice;
            Utils.depositPlayer(p.getPlayer(), finalCreationPrice, Callback.of(plugin,
                    result -> p.sendMessage(LangUtils.getMessage(Message.REIMBURSEMENT_RECEIVED,
                            new Replacement(Placeholder.AMOUNT, finalCreationPrice)
                    )),
                    error -> p.sendMessage(LangUtils.getMessage(Message.ERROR_OCCURRED,
                            new Replacement(Placeholder.ERROR, error::getLocalizedMessage)
                    ))
            ));

            if (bank.isPlayerBank()) {
                OfflinePlayer bankOwner = bank.getOwner();
                Utils.withdrawPlayer(bankOwner, finalCreationPrice, Callback.of(plugin,
                        result -> Messenger.notify(bankOwner, LangUtils.getMessage(Message.REIMBURSEMENT_PAID,
                                new Replacement(Placeholder.PLAYER, () -> account.getOwner().getName()),
                                new Replacement(Placeholder.AMOUNT, finalCreationPrice)
                        )),
                        error -> Messenger.notify(bankOwner, LangUtils.getMessage(Message.ERROR_OCCURRED,
                                new Replacement(Placeholder.ERROR, error::getLocalizedMessage)
                        ))
                ));
            }
        }

        p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_REMOVED, new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)));
        plugin.getAccountUtils().removeAccount(account, true);
        ClickType.removePlayerClickType(p);
        plugin.debug("Removed account (#" + account.getID() + ")");
    }

}
