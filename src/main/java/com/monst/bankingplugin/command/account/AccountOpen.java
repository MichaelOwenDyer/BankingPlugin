package com.monst.bankingplugin.command.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.PlayerSubCommand;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.entity.geo.location.AccountLocation;
import com.monst.bankingplugin.event.account.AccountOpenCommandEvent;
import com.monst.bankingplugin.event.account.AccountOpenEvent;
import com.monst.bankingplugin.exception.CancelledException;
import com.monst.bankingplugin.exception.ExecutionException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.command.ClickAction;
import com.monst.bankingplugin.util.Permission;
import com.monst.bankingplugin.util.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

import java.math.BigDecimal;

public class AccountOpen extends PlayerSubCommand {

    AccountOpen(BankingPlugin plugin) {
		super(plugin, "open");
    }

    @Override
    protected Permission getPermission() {
        return Permission.ACCOUNT_OPEN;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_OPEN;
    }

    @Override
    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION_ACCOUNT_OPEN;
    }

    @Override
    protected void execute(Player player, String[] args) throws ExecutionException, CancelledException {
        long limit = getPermissionLimit(player, Permission.ACCOUNT_NO_LIMIT, plugin.config().defaultAccountLimit.get());
        if (limit >= 0 && plugin.getAccountService().countByOwner(player) >= limit)
            throw new ExecutionException(plugin, Message.ACCOUNT_LIMIT_REACHED.with(Placeholder.LIMIT).as(limit));

        new AccountOpenCommandEvent(player, args).fire();

        plugin.debugf("%s can now click a chest to open an account", player.getName());
        player.sendMessage(Message.CLICK_CHEST_OPEN.translate(plugin));
        ClickAction.setBlockClickAction(player, block -> create(player, block));
    }

    /**
     * Creates a new account at the specified block.
     *
     * @param player  Player who executed the command
     * @param block  Clicked chest block to create the account at
     */
    private void create(Player player, Block block) throws ExecutionException {
        ClickAction.remove(player);
        if (plugin.getAccountService().isAccount(block))
            throw new ExecutionException(plugin, Message.CHEST_ALREADY_ACCOUNT);

        InventoryHolder ih = ((Chest) block.getState()).getInventory().getHolder();
        AccountLocation accountLocation = AccountLocation.toAccountLocation(ih);

        if (accountLocation.isBlocked())
            throw new ExecutionException(plugin, Message.CHEST_BLOCKED);

        Bank bank = plugin.getBankService().findContaining(accountLocation);
        if (bank == null)
            throw new ExecutionException(plugin, Message.CHEST_NOT_IN_BANK);

        if (!plugin.config().allowSelfBanking.get() && bank.isOwner(player))
            throw new ExecutionException(plugin, Message.NO_SELF_BANKING.with(Placeholder.BANK_NAME).as(bank.getColorizedName()));

        int playerAccountLimit = plugin.config().playerBankAccountLimit.at(bank);
        if (playerAccountLimit > 0 && plugin.getAccountService().findByBankAndOwner(bank, player).size() >= playerAccountLimit)
            throw new ExecutionException(plugin, Message.ACCOUNT_LIMIT_AT_BANK_REACHED
                    .with(Placeholder.BANK_NAME).as(bank.getColorizedName())
                    .and(Placeholder.LIMIT).as(playerAccountLimit));

        Account account = new Account(player, accountLocation);
        account.setRemainingOfflinePayouts(plugin.config().allowedOfflinePayouts.at(bank));

        try {
            new AccountOpenEvent(player, account).fire();
        } catch (CancelledException e) {
            if (Permission.ACCOUNT_CREATE_PROTECTED.notOwnedBy(player))
                throw new ExecutionException(plugin, Message.NO_PERMISSION_ACCOUNT_OPEN_PROTECTED);
        }

        BigDecimal creationPrice;
        if (bank.isOwner(player))
            creationPrice = BigDecimal.ZERO;
        else
            creationPrice = plugin.config().accountCreationPrice.at(bank).multiply(BigDecimal.valueOf(accountLocation.getSize()));

        if (creationPrice.signum() > 0) {
            final double finalCreationPrice = creationPrice.doubleValue();
            // Account owner pays the bank owner the creation fee
            if (!plugin.getPaymentService().withdraw(player, finalCreationPrice)) {
                double balance = plugin.getEconomy().getBalance(player);
                throw new ExecutionException(plugin, Message.ACCOUNT_CREATE_INSUFFICIENT_FUNDS
                        .with(Placeholder.PRICE).as(plugin.getEconomy().format(finalCreationPrice))
                        .and(Placeholder.PLAYER_BALANCE).as(plugin.getEconomy().format(balance))
                        .and(Placeholder.AMOUNT_REMAINING).as(plugin.getEconomy().format(
                                creationPrice.subtract(BigDecimal.valueOf(balance)).doubleValue())
                        ));
            }
            player.sendMessage(Message.ACCOUNT_CREATE_FEE_PAID
                    .with(Placeholder.PRICE).as(plugin.getEconomy().format(finalCreationPrice))
                    .and(Placeholder.BANK_NAME).as(bank.getColorizedName())
                    .translate(plugin));
            // Bank owner receives the payment from the customer
            if (bank.isPlayerBank()) {
                OfflinePlayer bankOwner = bank.getOwner();
                if (plugin.getPaymentService().deposit(bankOwner, finalCreationPrice))
                    Utils.message(bankOwner, Message.ACCOUNT_CREATE_FEE_RECEIVED
                            .with(Placeholder.PLAYER).as(player.getName())
                            .and(Placeholder.AMOUNT).as(plugin.getEconomy().format(finalCreationPrice))
                            .and(Placeholder.BANK_NAME).as(bank.getColorizedName())
                            .translate(plugin));
            }
        }

        account.updateChestTitle();
        bank.addAccount(account);
        plugin.getAccountService().appraise(account);
        plugin.getAccountService().save(account);
        plugin.getBankService().update(bank);
        plugin.debug("Account opened.");
        player.sendMessage(Message.ACCOUNT_OPENED.with(Placeholder.BANK_NAME).as(bank.getColorizedName()).translate(plugin));
    }
}
