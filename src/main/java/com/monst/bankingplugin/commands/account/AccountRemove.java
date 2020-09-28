package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.banking.bank.BankField;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountPreRemoveEvent;
import com.monst.bankingplugin.events.account.AccountRemoveEvent;
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
        return hasPermission(sender, Permissions.ACCOUNT_CREATE) ? Messages.COMMAND_USAGE_ACCOUNT_REMOVE : "";
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
        sender.sendMessage(String.format(Messages.CLICK_ACCOUNT_CHEST, "remove"));
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
        if (!confirm(p, account))
            return;

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

        if (creationPrice > 0 && account.isOwner(p) && !account.getBank().isOwner(p)) {

            double finalCreationPrice = creationPrice;
            Utils.depositPlayer(p.getPlayer(), account.getLocation().getWorld().getName(), finalCreationPrice,
                    Callback.of(plugin,
                            result -> p.sendMessage(String.format(Messages.ACCOUNT_REIMBURSEMENT_RECEIVED,
                                    Utils.format(finalCreationPrice))),
                            error -> p.sendMessage(Messages.ERROR_OCCURRED)));

            if (account.getBank().isPlayerBank()) {
                OfflinePlayer bankOwner = account.getBank().getOwner();
                Utils.withdrawPlayer(bankOwner, account.getLocation().getWorld().getName(), finalCreationPrice,
                        Callback.of(plugin,
                                result -> Utils.notifyPlayers(String.format(Messages.ACCOUNT_REIMBURSEMENT_PAID,
                                        account.getOwner().getName(), Utils.format(finalCreationPrice)),
                                        bankOwner),
                                error -> Utils.notifyPlayers(Messages.ERROR_OCCURRED, bankOwner)));
            }
        }
        p.sendMessage(Messages.ACCOUNT_REMOVED);
        plugin.getAccountUtils().removeAccount(account, true);
        plugin.debug("Removed account (#" + account.getID() + ")");
    }

    private boolean confirm(Player p, Account account) {

        if (!account.isOwner(p) && !p.hasPermission(Permissions.ACCOUNT_REMOVE_OTHER) && !account.getBank().isTrusted(p)) {
            if (account.isTrusted(p))
                p.sendMessage(Messages.MUST_BE_OWNER);
            else
                p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_REMOVE_OTHER);
            return !hasEntry(p);
        }

        if ((account.getBalance().signum() > 0 || Config.confirmOnRemove)) {
            if (!isConfirmed(p, account.getID())) {
                plugin.debug("Needs confirmation");
                if (account.getBalance().signum() > 0) {
                    p.sendMessage(Messages.ACCOUNT_BALANCE_NOT_ZERO);
                }
                p.sendMessage(Messages.CLICK_TO_CONFIRM);
                return false;
            }
        } else
            ClickType.removePlayerClickType(p);
        return true;
    }

}
