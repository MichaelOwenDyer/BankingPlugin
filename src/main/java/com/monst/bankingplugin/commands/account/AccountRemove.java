package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountRemoveCommandEvent;
import com.monst.bankingplugin.events.account.AccountRemoveEvent;
import com.monst.bankingplugin.lang.*;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.PayrollOffice;
import com.monst.bankingplugin.utils.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AccountRemove extends SubCommand.AccountSubCommand implements ConfirmableAccountAction {

    private static AccountRemove instance;

    public static AccountRemove getInstance() {
        return instance;
    }

    AccountRemove(BankingPlugin plugin) {
		super(plugin, "remove", true);
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

        AccountRemoveCommandEvent event = new AccountRemoveCommandEvent((Player) sender, args);
        event.fire();
        if (event.isCancelled()) {
            plugin.debug("Account remove command event cancelled");
            return true;
        }

        plugin.debug(sender.getName() + " can now click a chest to remove an account");
        sender.sendMessage(Messages.get(Message.CLICK_ACCOUNT_REMOVE));
        ClickType.setRemoveClickType((Player) sender);
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
                p.sendMessage(Messages.get(Message.MUST_BE_OWNER));
            else
                p.sendMessage(Messages.get(Message.NO_PERMISSION_ACCOUNT_REMOVE_OTHER));
            if (!hasEntry(p))
                ClickType.removeClickType(p);
            return;
        }

        if (account.getBalance().signum() > 0 || Config.confirmOnRemove.get()) {
            if (!isConfirmed(p, account.getID())) {
                plugin.debug("Needs confirmation");
                if (account.getBalance().signum() > 0) {
                    p.sendMessage(Messages.get(Message.ACCOUNT_BALANCE_NOT_ZERO,
                            new Replacement(Placeholder.ACCOUNT_BALANCE, account::getBalance)
                    ));
                }
                p.sendMessage(Messages.get(Message.CLICK_AGAIN_TO_CONFIRM));
                return;
            }
        }

        plugin.debugf("%s is removing %s account (#%d)",
                p.getName(),
                account.isOwner(p) ? "their" : account.getOwner().getName() + "'s",
                account.getID());

        AccountRemoveEvent event = new AccountRemoveEvent(p, account);
        event.fire();
        if (event.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_REMOVE_PROTECTED)) {
            plugin.debug("Remove event cancelled (#" + account.getID() + ")");
            p.sendMessage(Messages.get(Message.NO_PERMISSION_ACCOUNT_REMOVE_PROTECTED));
            return;
        }

        Bank bank = account.getBank();
        if (account.isOwner(p) && bank.getReimburseAccountCreation().get()) {
            double reimbursement = bank.getAccountCreationPrice().get();
            reimbursement *= account.getSize();
            reimbursement *= bank.isOwner(p) ? 0 : 1;

            if (reimbursement > 0) {
                if (PayrollOffice.deposit(p, reimbursement))
                    p.sendMessage(Messages.get(Message.REIMBURSEMENT_RECEIVED,
                            new Replacement(Placeholder.AMOUNT, reimbursement)
                    ));
                if (bank.isPlayerBank() && PayrollOffice.withdraw(bank.getOwner(), reimbursement)) {
                    Mailman.notify(bank.getOwner(), Messages.get(Message.REIMBURSEMENT_PAID,
                            new Replacement(Placeholder.PLAYER, account.getOwnerName()),
                            new Replacement(Placeholder.AMOUNT, reimbursement)
                    ));
                }
            }
        }

        p.sendMessage(Messages.get(Message.ACCOUNT_REMOVED, new Replacement(Placeholder.BANK_NAME, bank::getColorizedName)));
        plugin.getAccountRepository().remove(account, true);
        plugin.debug("Removed account (#" + account.getID() + ")");
    }

}
