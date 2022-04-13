package com.monst.bankingplugin.command.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.PlayerSubCommand;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.entity.geo.location.AccountLocation;
import com.monst.bankingplugin.event.account.AccountMigrateCommandEvent;
import com.monst.bankingplugin.event.account.AccountMigrateEvent;
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

import java.math.BigDecimal;
import java.util.Objects;

public class AccountMigrate extends PlayerSubCommand {

    AccountMigrate(BankingPlugin plugin) {
		super(plugin, "migrate");
    }

    @Override
    protected Permission getPermission() {
        return Permission.ACCOUNT_OPEN;
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
    protected void execute(Player player, String[] args) throws CancelledException {
        new AccountMigrateCommandEvent(player, args).fire();
        player.sendMessage(Message.CLICK_ACCOUNT_MIGRATE.translate(plugin));
        ClickAction.setAccountClickAction(player, account -> selectAccount(player, account));
        plugin.debugf("%s is migrating an account", player.getName());
    }

    private void selectAccount(Player player, Account accountToMove) throws ExecutionException {
        ClickAction.remove(player);
        if (!accountToMove.isOwner(player) && Permission.ACCOUNT_MIGRATE_OTHER.notOwnedBy(player)) {
            if (accountToMove.isTrusted(player))
                throw new ExecutionException(plugin, Message.MUST_BE_OWNER);
            throw new ExecutionException(plugin, Message.NO_PERMISSION_ACCOUNT_MIGRATE_OTHER);
        }

        plugin.debugf("%s wants to migrate account #%d", player.getName(), accountToMove.getID());
        ClickAction.setBlockClickAction(player, block -> selectNewChest(player, accountToMove.getID(), block));
        player.sendMessage(Message.CLICK_CHEST_MIGRATE.translate(plugin));
    }

    private void selectNewChest(Player player, int toMoveID, Block targetBlock) throws ExecutionException {
        ClickAction.remove(player);
        Account clickedAccount = plugin.getAccountService().findAt(targetBlock);
        if (clickedAccount != null) {
            if (clickedAccount.getID() == toMoveID)
                throw new ExecutionException(plugin, Message.SAME_CHEST);
            throw new ExecutionException(plugin, Message.CHEST_ALREADY_ACCOUNT);
        }

        Chest c = (Chest) targetBlock.getState();
        AccountLocation newAccountLocation = AccountLocation.toAccountLocation(c.getInventory().getHolder());

        if (newAccountLocation.isBlocked())
            throw new ExecutionException(plugin, Message.CHEST_BLOCKED);

        Bank newBank = plugin.getBankService().findContaining(newAccountLocation);
        if (newBank == null)
            throw new ExecutionException(plugin, Message.CHEST_NOT_IN_BANK);

        Account accountToMove = plugin.getAccountService().findByID(toMoveID);

        if (!Objects.equals(accountToMove.getBank(), newBank) && Permission.ACCOUNT_MIGRATE_BANK.notOwnedBy(player))
            throw new ExecutionException(plugin, Message.NO_PERMISSION_ACCOUNT_MIGRATE_BANK);

        try {
            new AccountMigrateEvent(player, accountToMove, newAccountLocation).fire();
        } catch (CancelledException e) {
            if (Permission.ACCOUNT_CREATE_PROTECTED.notOwnedBy(player))
                throw new ExecutionException(plugin, Message.NO_PERMISSION_ACCOUNT_OPEN_PROTECTED);
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
            throw new ExecutionException(plugin, Message.ACCOUNT_CREATE_INSUFFICIENT_FUNDS
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
            if (oldBank.isPlayerBank() && plugin.getPaymentService().withdraw(oldOwner, finalReimbursement))
                Utils.message(oldOwner, Message.ACCOUNT_REIMBURSEMENT_PAID
                        .with(Placeholder.PLAYER).as(player.getName())
                        .and(Placeholder.AMOUNT).as(plugin.getEconomy().format(finalReimbursement))
                        .translate(plugin));
        }
        if (creationPrice.signum() > 0) {
            double finalCreationPrice = creationPrice.doubleValue();
            // Account owner pays creation fee for new account
            player.sendMessage(Message.ACCOUNT_CREATE_FEE_PAID
                    .with(Placeholder.PRICE).as(plugin.getEconomy().format(finalCreationPrice))
                    .and(Placeholder.BANK_NAME).as(newBank.getColorizedName())
                    .translate(plugin));
            // Bank owner of new account receives account creation fee
            if (newBank.isPlayerBank() && plugin.getPaymentService().deposit(newBank.getOwner(), finalCreationPrice))
                Utils.message(newBank.getOwner(), Message.ACCOUNT_CREATE_FEE_RECEIVED
                        .with(Placeholder.PLAYER).as(player.getName())
                        .and(Placeholder.AMOUNT).as(plugin.getEconomy().format(finalCreationPrice))
                        .and(Placeholder.BANK_NAME).as(newBank.getColorizedName())
                        .translate(plugin));
        }

        accountToMove.resetChestTitle();
        accountToMove.setLocation(newAccountLocation);
        oldBank.removeAccount(accountToMove);
        newBank.addAccount(accountToMove);
        accountToMove.updateChestTitle();
        plugin.getAccountService().update(accountToMove);
        player.sendMessage(Message.ACCOUNT_MIGRATED.translate(plugin));
        plugin.debugf("Migrated account #%d", accountToMove.getID());
    }

}
