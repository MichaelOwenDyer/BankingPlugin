package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountRemoveCommandEvent;
import com.monst.bankingplugin.events.account.AccountRemoveEvent;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class AccountRemove extends SubCommand {

    AccountRemove(BankingPlugin plugin) {
		super(plugin, "remove", true);
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
        plugin.debugf("%s wants to remove an account", sender.getName());

        AccountRemoveCommandEvent event = new AccountRemoveCommandEvent((Player) sender, args);
        event.fire();
        if (event.isCancelled()) {
            plugin.debug("Account remove command event cancelled");
            return true;
        }

        plugin.debugf("%s can now click a chest to remove an account", sender.getName());
        sender.sendMessage(Message.CLICK_ACCOUNT_REMOVE.translate());
        ClickType.setRemoveClickType((Player) sender);
        return true;
    }

    /**
     * Removes an account
     *
     * @param p       Player who executed the command
     * @param account Account to be removed
     */
    public static void remove(BankingPlugin plugin, Player p, Account account) {
        if (!account.isOwner(p) && !p.hasPermission(Permissions.ACCOUNT_REMOVE_OTHER) && !account.getBank().isTrusted(p)) {
            Message message = account.isTrusted(p) ? Message.MUST_BE_OWNER : Message.NO_PERMISSION_ACCOUNT_REMOVE_OTHER;
            p.sendMessage(message.translate());
            ClickType.removeClickType(p);
            return;
        }

        boolean balanceRemaining = account.getBalance().signum() > 0;
        if ((balanceRemaining || Config.confirmOnRemove.get()) && ClickType.needsConfirmation(p)) {
            plugin.debug("Account removal needs confirmation");
            if (balanceRemaining)
                p.sendMessage(Message.ACCOUNT_BALANCE_NOT_ZERO
                        .with(Placeholder.ACCOUNT_BALANCE).as(account.getBalance())
                        .translate());
            p.sendMessage(Message.CLICK_AGAIN_TO_CONFIRM.translate());
            ClickType.confirmClickType(p);
            return;
        }
        ClickType.removeClickType(p);

        plugin.debugf("%s is removing account #%d", p.getName(), account.getID());

        AccountRemoveEvent event = new AccountRemoveEvent(p, account);
        event.fire();
        if (event.isCancelled() && !p.hasPermission(Permissions.ACCOUNT_REMOVE_PROTECTED)) {
            plugin.debugf("Remove event cancelled at account #%d", account.getID());
            p.sendMessage(Message.NO_PERMISSION_ACCOUNT_REMOVE_PROTECTED.translate());
            return;
        }

        Bank bank = account.getBank();
        if (account.isOwner(p) && bank.reimburseAccountCreation().get()) {
            BigDecimal reimbursement;
            if (bank.isOwner(p))
                reimbursement = BigDecimal.ZERO;
            else
                reimbursement = bank.accountCreationPrice().get().multiply(BigDecimal.valueOf(account.getSize()));

            if (reimbursement.signum() > 0) {
                if (PayrollOffice.deposit(p, reimbursement))
                    p.sendMessage(Message.REIMBURSEMENT_RECEIVED.with(Placeholder.AMOUNT).as(reimbursement).translate());
                if (bank.isPlayerBank() && PayrollOffice.withdraw(bank.getOwner(), reimbursement)) {
                    Utils.notify(bank.getOwner(), Message.REIMBURSEMENT_PAID
                            .with(Placeholder.PLAYER).as(account.getOwnerDisplayName())
                            .and(Placeholder.AMOUNT).as(reimbursement)
                            .translate());
                }
            }
        }

        p.sendMessage(Message.ACCOUNT_REMOVED.with(Placeholder.BANK_NAME).as(bank.getColorizedName()).translate());
        plugin.getAccountRepository().remove(account, true);
        plugin.debugf("Removed account #%d", account.getID());
    }

}
