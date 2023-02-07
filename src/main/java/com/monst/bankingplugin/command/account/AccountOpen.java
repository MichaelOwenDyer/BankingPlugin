package com.monst.bankingplugin.command.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.ClickAction;
import com.monst.bankingplugin.command.Permission;
import com.monst.bankingplugin.command.PlayerSubCommand;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.entity.geo.location.AccountLocation;
import com.monst.bankingplugin.event.account.AccountOpenCommandEvent;
import com.monst.bankingplugin.event.account.AccountOpenEvent;
import com.monst.bankingplugin.exception.CommandExecutionException;
import com.monst.bankingplugin.exception.EventCancelledException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.command.Permissions;
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
        return Permissions.ACCOUNT_OPEN;
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
    protected void execute(Player player, String[] args) throws CommandExecutionException, EventCancelledException {
        long limit = PlayerSubCommand.getPermissionLimit(player, Permissions.ACCOUNT_NO_LIMIT, plugin.config().defaultAccountLimit.get());
        if (limit >= 0 && plugin.getAccountService().countByOwner(player) >= limit)
            throw err(Message.ACCOUNT_LIMIT_REACHED.with(Placeholder.LIMIT).as(limit));

        new AccountOpenCommandEvent(player, args).fire();

        plugin.debugf("%s can now click a chest to open an account", player.getName());
        player.sendMessage(Message.CLICK_CHEST_OPEN.translate(plugin));
        ClickAction.setBlockClickAction(player, chest -> create(player, chest));
    }

    /**
     * Creates a new account at the specified block.
     *
     * @param player  Player who executed the command
     * @param chest  Clicked chest to create the account at
     */
    private void create(Player player, Block chest) throws CommandExecutionException {
        ClickAction.remove(player);
        if (plugin.getAccountService().isAccount(chest))
            throw err(Message.CHEST_ALREADY_ACCOUNT);

        InventoryHolder ih = ((Chest) chest.getState()).getInventory().getHolder();
        AccountLocation accountLocation = AccountLocation.from(ih);

        Bank bank = plugin.getBankService().findContaining(accountLocation);
        if (bank == null)
            throw err(Message.CHEST_NOT_IN_BANK);

        if (!plugin.config().allowSelfBanking.get() && bank.isOwner(player))
            throw err(Message.NO_SELF_BANKING.with(Placeholder.BANK_NAME).as(bank.getColorizedName()));

        int playerAccountLimit = plugin.config().playerBankAccountLimit.at(bank);
        if (playerAccountLimit > 0 && plugin.getAccountService().countByBankAndOwner(bank, player) >= playerAccountLimit)
            throw err(Message.ACCOUNT_LIMIT_AT_BANK_REACHED
                    .with(Placeholder.BANK_NAME).as(bank.getColorizedName())
                    .and(Placeholder.LIMIT).as(playerAccountLimit));

        Account account = new Account(bank, player, accountLocation);
        account.setRemainingOfflinePayouts(plugin.config().allowedOfflinePayouts.at(bank));

        try {
            new AccountOpenEvent(player, account).fire();
        } catch (EventCancelledException e) {
            if (Permissions.ACCOUNT_CREATE_PROTECTED.notOwnedBy(player))
                throw err(Message.NO_PERMISSION_ACCOUNT_OPEN_PROTECTED);
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
                throw err(Message.ACCOUNT_CREATE_INSUFFICIENT_FUNDS
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
                if (plugin.getPaymentService().deposit(bankOwner, finalCreationPrice)) {
                    if (bankOwner.isOnline()) {
                        bankOwner.getPlayer().sendMessage(Message.ACCOUNT_CREATE_FEE_RECEIVED
                                .with(Placeholder.PLAYER).as(player.getName())
                                .and(Placeholder.AMOUNT).as(plugin.getEconomy().format(finalCreationPrice))
                                .and(Placeholder.BANK_NAME).as(bank.getColorizedName())
                                .translate(plugin));
                    }
                }
            }
        }
    
        bank.addAccount(account);
        account.updateChestTitle();
        account.setBalance(plugin.getWorths().appraise(account));
        plugin.getAccountService().save(account);
        plugin.debugf("Created account %s", account);
        player.sendMessage(Message.ACCOUNT_OPENED.with(Placeholder.BANK_NAME).as(bank.getColorizedName()).translate(plugin));
    }
}
