package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountRemoveCommandEvent;
import com.monst.bankingplugin.events.account.AccountRemoveEvent;
import com.monst.bankingplugin.lang.*;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.PayrollOffice;
import com.monst.bankingplugin.utils.Permissions;
import org.bukkit.Bukkit;
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
    protected String getPermission() {
        return Permissions.ACCOUNT_CREATE;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_REMOVE;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " wants to remove an account");

        AccountRemoveCommandEvent event = new AccountRemoveCommandEvent(((Player) sender));
        event.fire();
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

        if (account.getBalance().signum() > 0 || Config.confirmOnRemove.get()) {
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
        event.fire();
        if (event.isCancelled()) {
            plugin.debug("Remove event cancelled (#" + account.getID() + ")");
            return;
        }

        Bank bank = account.getBank();
        if (account.isOwner(p) && bank.getReimburseAccountCreation().get()) {
            double reimbursement = bank.getAccountCreationPrice().get();
            reimbursement *= account.getSize();
            reimbursement *= bank.isOwner(p) ? 0 : 1;

            if (reimbursement > 0) {
                if (PayrollOffice.deposit(p, reimbursement))
                    p.sendMessage(LangUtils.getMessage(Message.REIMBURSEMENT_RECEIVED,
                            new Replacement(Placeholder.AMOUNT, reimbursement)
                    ));
                if (bank.isPlayerBank() && PayrollOffice.withdraw(bank.getOwner(), reimbursement)) {
                    Mailman.notify(bank.getOwner(), LangUtils.getMessage(Message.REIMBURSEMENT_PAID,
                            new Replacement(Placeholder.PLAYER, account.getOwnerName()),
                            new Replacement(Placeholder.AMOUNT, reimbursement)
                    ));
                }
            }
        }

        p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_REMOVED, new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)));
        plugin.getAccountRepository().remove(account, true);
        ClickType.removePlayerClickType(p);
        plugin.debug("Removed account (#" + account.getID() + ")");
    }

}
