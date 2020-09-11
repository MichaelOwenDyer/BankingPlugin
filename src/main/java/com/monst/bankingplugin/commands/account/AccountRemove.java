package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.banking.bank.BankField;
import com.monst.bankingplugin.events.account.AccountPreRemoveEvent;
import com.monst.bankingplugin.events.account.AccountRemoveEvent;
import com.monst.bankingplugin.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AccountRemove extends AccountCommand.SubCommand {

    AccountRemove() {
        super("remove", true);
    }

    @Override
    protected String getHelpMessage(CommandSender sender) {
        return hasPermission(sender, Permissions.ACCOUNT_CREATE) ? Messages.COMMAND_USAGE_ACCOUNT_REMOVE : "";
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        plugin.debug(p.getName() + " wants to remove an account");

        AccountPreRemoveEvent event = new AccountPreRemoveEvent(p);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Account pre-remove event cancelled");
            return true;
        }

        plugin.debug(p.getName() + " can now click a chest to remove an account");
        p.sendMessage(String.format(Messages.CLICK_ACCOUNT_CHEST, "remove"));
        ClickType.setPlayerClickType(p, ClickType.remove());
        return true;
    }

    /**
     * Remove an account
     *
     * @param executor Player who executed the command and will receive the message
     * @param account  Account to be removed
     */
    public static void remove(Player executor, Account account) {
        plugin.debugf("%s is removing %s account (#%d)",
                executor.getName(),
                account.isOwner(executor) ? "their" : account.getOwner().getName() + "'s",
                account.getID());

        AccountRemoveEvent event = new AccountRemoveEvent(executor, account);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Remove event cancelled (#" + account.getID() + ")");
            return;
        }

        Bank bank = account.getBank();
        double creationPrice = bank.get(BankField.ACCOUNT_CREATION_PRICE);
        creationPrice *= account.getSize();
        creationPrice *= bank.get(BankField.REIMBURSE_ACCOUNT_CREATION) ? 1 : 0;

        if (creationPrice > 0 && account.isOwner(executor) && !account.getBank().isOwner(executor)) {

            double finalCreationPrice = creationPrice;
            Utils.depositPlayer(executor.getPlayer(), account.getLocation().getWorld().getName(), finalCreationPrice,
                    Callback.of(plugin,
                            result -> executor.sendMessage(String.format(Messages.ACCOUNT_REIMBURSEMENT_RECEIVED,
                                    Utils.format(finalCreationPrice))),
                            throwable -> executor.sendMessage(Messages.ERROR_OCCURRED)));

            if (account.getBank().isPlayerBank()) {
                OfflinePlayer bankOwner = account.getBank().getOwner();
                Utils.withdrawPlayer(bankOwner, account.getLocation().getWorld().getName(), finalCreationPrice,
                        Callback.of(plugin,
                                result -> Utils.notifyPlayers(String.format(Messages.ACCOUNT_REIMBURSEMENT_PAID,
                                        account.getOwner().getName(), Utils.format(finalCreationPrice)),
                                        bankOwner),
                                throwable -> Utils.notifyPlayers(Messages.ERROR_OCCURRED, bankOwner)));
            }
        }

        executor.sendMessage(Messages.ACCOUNT_REMOVED);
        plugin.getAccountUtils().removeAccount(account, true);
        plugin.debug("Removed account (#" + account.getID() + ")");
    }

}
