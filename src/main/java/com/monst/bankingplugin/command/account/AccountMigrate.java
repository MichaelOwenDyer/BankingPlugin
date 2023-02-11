package com.monst.bankingplugin.command.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.ClickAction;
import com.monst.bankingplugin.command.Permission;
import com.monst.bankingplugin.command.PlayerSubCommand;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.entity.geo.location.AccountLocation;
import com.monst.bankingplugin.event.account.AccountMigrateCommandEvent;
import com.monst.bankingplugin.event.account.AccountMigrateEvent;
import com.monst.bankingplugin.exception.CommandExecutionException;
import com.monst.bankingplugin.exception.EventCancelledException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.command.Permissions;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Objects;

public class AccountMigrate extends PlayerSubCommand {

    AccountMigrate(BankingPlugin plugin) {
		super(plugin, "migrate");
    }

    @Override
    protected Permission getPermission() {
        return Permissions.ACCOUNT_OPEN;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_MIGRATE;
    }

    @Override
    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION_ACCOUNT_MIGRATE;
    }

    @Override
    protected void execute(Player player, String[] args) throws EventCancelledException {
        new AccountMigrateCommandEvent(player, args).fire();
        player.sendMessage(Message.CLICK_ACCOUNT_MIGRATE.translate(plugin));
        ClickAction.setAccountClickAction(player, account -> selectAccount(player, account));
        plugin.debug("%s must click a chest to migrate.", player.getName());
    }

    private void selectAccount(Player player, Account accountToMove) throws CommandExecutionException {
        ClickAction.remove(player);
        if (!accountToMove.isOwner(player) && Permissions.ACCOUNT_MIGRATE_OTHER.notOwnedBy(player)) {
            if (accountToMove.isTrusted(player))
                throw err(Message.MUST_BE_OWNER);
            throw err(Message.NO_PERMISSION_ACCOUNT_MIGRATE_OTHER);
        }

        plugin.debug("%s wants to migrate account %s", player.getName(), accountToMove);
        ClickAction.setBlockClickAction(player, chest -> selectNewChest(player, accountToMove, chest));
        player.sendMessage(Message.CLICK_CHEST_MIGRATE.translate(plugin));
    }

    private void selectNewChest(Player player, Account accountToMove, Block targetChest) throws CommandExecutionException {
        ClickAction.remove(player);
        Account clickedAccount = plugin.getAccountService().findAtChest(targetChest);
        if (clickedAccount != null) {
            if (clickedAccount.equals(accountToMove))
                throw err(Message.SAME_CHEST);
            throw err(Message.CHEST_ALREADY_ACCOUNT);
        }

        Chest c = (Chest) targetChest.getState();
        AccountLocation newAccountLocation = AccountLocation.from(c.getInventory().getHolder());

        Bank newBank = plugin.getBankService().findContaining(newAccountLocation);
        if (newBank == null)
            throw err(Message.CHEST_NOT_IN_BANK);

        if (!Objects.equals(accountToMove.getBank(), newBank) && Permissions.ACCOUNT_MIGRATE_BANK.notOwnedBy(player))
            throw err(Message.NO_PERMISSION_ACCOUNT_MIGRATE_BANK);

        try {
            new AccountMigrateEvent(player, accountToMove, newAccountLocation).fire();
        } catch (EventCancelledException e) {
            if (Permissions.ACCOUNT_CREATE_PROTECTED.notOwnedBy(player))
                throw err(Message.NO_PERMISSION_ACCOUNT_OPEN_PROTECTED);
        }

        Bank oldBank = accountToMove.getBank();

        BigDecimal creationPrice;
        if (newBank.isOwner(player))
            creationPrice = BigDecimal.ZERO;
        else
            creationPrice = plugin.config().accountCreationPrice.at(newBank).multiply(BigDecimal.valueOf(newAccountLocation.getSize()));

        BigDecimal reimbursement;
        if (oldBank.isOwner(player) || !plugin.config().reimburseAccountCreation.at(oldBank))
            reimbursement = BigDecimal.ZERO;
        else
            reimbursement = plugin.config().accountCreationPrice.at(oldBank).multiply(BigDecimal.valueOf(accountToMove.getSize()));

        BigDecimal difference = reimbursement.subtract(creationPrice);
        double finalDifference = difference.doubleValue();
        if (!plugin.getPaymentService().transact(player, finalDifference)) {
            double balance = plugin.getEconomy().getBalance(player);
            throw err(Message.ACCOUNT_CREATE_INSUFFICIENT_FUNDS
                    .with(Placeholder.PRICE).as(plugin.getEconomy().format(finalDifference))
                    .and(Placeholder.PLAYER_BALANCE).as(plugin.getEconomy().format(balance))
                    .and(Placeholder.AMOUNT_REMAINING).as(plugin.getEconomy().format(
                            difference.abs().subtract(BigDecimal.valueOf(balance)).doubleValue())
                    ));
        }

        if (reimbursement.signum() > 0) {
            double finalReimbursement = reimbursement.doubleValue();
            // Customer receives reimbursement for old account
            player.sendMessage(Message.ACCOUNT_REIMBURSEMENT_RECEIVED
                    .with(Placeholder.AMOUNT).as(plugin.getEconomy().format(finalReimbursement))
                    .translate(plugin));
            // Bank owner of old account pays reimbursement
            OfflinePlayer oldOwner = oldBank.getOwner();
            if (oldBank.isPlayerBank() && plugin.getPaymentService().withdraw(oldOwner, finalReimbursement)) {
                if (oldOwner.isOnline()) {
                    oldOwner.getPlayer().sendMessage(Message.ACCOUNT_REIMBURSEMENT_PAID
                            .with(Placeholder.PLAYER).as(player.getName())
                            .and(Placeholder.AMOUNT).as(plugin.getEconomy().format(finalReimbursement))
                            .translate(plugin));
                }
            }
        }
        if (creationPrice.signum() > 0) {
            double finalCreationPrice = creationPrice.doubleValue();
            // Account owner pays creation fee for new account
            player.sendMessage(Message.ACCOUNT_CREATE_FEE_PAID
                    .with(Placeholder.PRICE).as(plugin.getEconomy().format(finalCreationPrice))
                    .and(Placeholder.BANK_NAME).as(newBank.getColorizedName())
                    .translate(plugin));
            // Bank owner of new account receives account creation fee
            if (newBank.isPlayerBank() && plugin.getPaymentService().deposit(newBank.getOwner(), finalCreationPrice)) {
                if (newBank.getOwner().isOnline()) {
                    newBank.getOwner().getPlayer().sendMessage(Message.ACCOUNT_CREATE_FEE_RECEIVED
                            .with(Placeholder.PLAYER).as(player.getName())
                            .and(Placeholder.AMOUNT).as(plugin.getEconomy().format(finalCreationPrice))
                            .and(Placeholder.BANK_NAME).as(newBank.getColorizedName())
                            .translate(plugin));
                }
            }
        }

        accountToMove.resetChestTitle();
        accountToMove.setLocation(newAccountLocation);
        accountToMove.setBank(newBank);
        accountToMove.updateChestTitle();
        plugin.getAccountService().update(accountToMove);
        player.sendMessage(Message.ACCOUNT_MIGRATED.translate(plugin));
        plugin.debug("Migrated account %s", accountToMove);
    }

}
