package com.monst.bankingplugin.command.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.ClickAction;
import com.monst.bankingplugin.command.Permission;
import com.monst.bankingplugin.command.PlayerSubCommand;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.event.account.AccountCloseCommandEvent;
import com.monst.bankingplugin.event.account.AccountCloseEvent;
import com.monst.bankingplugin.command.CommandExecutionException;
import com.monst.bankingplugin.event.EventCancelledException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.command.Permissions;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class AccountClose extends PlayerSubCommand {

    AccountClose(BankingPlugin plugin) {
		super(plugin, "close");
    }

    @Override
    protected Permission getPermission() {
        return Permissions.ACCOUNT_OPEN;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_CLOSE;
    }

    @Override
    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION_ACCOUNT_CLOSE;
    }

    @Override
    protected void execute(Player player, String[] args) throws EventCancelledException {
        new AccountCloseCommandEvent(player, args).fire();

        plugin.debug("%s can now click a chest to close an account.", player.getName());
        player.sendMessage(Message.CLICK_ACCOUNT_CLOSE.translate(plugin));
        ClickAction.setAccountClickAction(player, account -> close(player, account));
    }

    /**
     * Removes an account
     *
     * @param player  Player who executed the command
     * @param account Account to be removed
     */
    private void close(Player player, Account account) throws CommandExecutionException {
        if (!account.isOwner(player) && Permissions.ACCOUNT_CLOSE_OTHER.notOwnedBy(player) && !account.getBank().isTrusted(player)) {
            ClickAction.remove(player);
            if (account.isTrusted(player))
                throw err(Message.MUST_BE_OWNER);
            throw err(Message.NO_PERMISSION_ACCOUNT_CLOSE_OTHER);
        }

        boolean balanceRemaining = account.getBalance().signum() > 0;
        if ((balanceRemaining || plugin.config().confirmOnRemove.get()) && ClickAction.mustConfirm(player)) {
            plugin.debug("Account removal needs confirmation.");
            if (balanceRemaining)
                player.sendMessage(Message.ACCOUNT_BALANCE_NOT_ZERO
                        .with(Placeholder.ACCOUNT_BALANCE).as(plugin.getEconomy().format(account.getBalance().doubleValue()))
                        .translate(plugin));
            player.sendMessage(Message.ABOUT_TO_CLOSE_ACCOUNT
                    .with(Placeholder.BANK_NAME).as(account.getBank().getName())
                    .and(Placeholder.ACCOUNT_ID).as(account.getID())
                    .translate(plugin));
            player.sendMessage(Message.CLICK_AGAIN_TO_CONFIRM.translate(plugin));
            return;
        }
        ClickAction.remove(player);

        plugin.debug("%s is removing account %s", player.getName(), account);

        new AccountCloseEvent(player, account).fire();

        Bank bank = account.getBank();
        if (account.isOwner(player) && plugin.config().reimburseAccountCreation.at(bank)) {
            BigDecimal reimbursement;
            if (bank.isOwner(player))
                reimbursement = BigDecimal.ZERO;
            else
                reimbursement = plugin.config().accountCreationPrice.at(bank).multiply(BigDecimal.valueOf(account.getSize()));

            if (reimbursement.signum() > 0) {
                final double finalReimbursement = reimbursement.doubleValue();
                if (plugin.getPaymentService().deposit(player, finalReimbursement)) {
                    player.sendMessage(Message.ACCOUNT_REIMBURSEMENT_RECEIVED
                            .with(Placeholder.AMOUNT).as(plugin.getEconomy().format(finalReimbursement))
                            .translate(plugin));
                    if (bank.isPlayerBank() && plugin.getPaymentService().withdraw(bank.getOwner(), finalReimbursement)) {
                        if (bank.getOwner().isOnline()) {
                            bank.getOwner().getPlayer().sendMessage(Message.ACCOUNT_REIMBURSEMENT_PAID
                                    .with(Placeholder.PLAYER).as(account.getOwner().getName())
                                    .and(Placeholder.AMOUNT).as(plugin.getEconomy().format(finalReimbursement))
                                    .translate(plugin));
                        }
                    }
                }
            }
        }

        account.resetChestTitle();
        bank.removeAccount(account);
        plugin.getAccountService().remove(account);
        player.sendMessage(Message.ACCOUNT_CLOSED.with(Placeholder.BANK_NAME).as(bank.getColorizedName()).translate(plugin));
        plugin.debug("Removed account %s", account);
    }

}
