package com.monst.bankingplugin.command.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.PlayerSubCommand;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.entity.geo.location.AccountLocation;
import com.monst.bankingplugin.event.account.AccountRecoverEvent;
import com.monst.bankingplugin.exception.CancelledException;
import com.monst.bankingplugin.exception.ExecutionException;
import com.monst.bankingplugin.gui.AccountRecoveryGUI;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.command.ClickAction;
import com.monst.bankingplugin.util.Permission;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

public class AccountRecover extends PlayerSubCommand {

    AccountRecover(BankingPlugin plugin) {
        super(plugin, "recover");
    }

    @Override
    protected Permission getPermission() {
        return Permission.ACCOUNT_RECOVER;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_RECOVER;
    }

    @Override
    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION_ACCOUNT_RECOVER;
    }

    @Override
    protected void execute(Player player, String[] args) {
        new AccountRecoveryGUI(plugin, account -> block -> recover(player, account.getID(), block)).open(player);
    }

    private void recover(Player player, int accountId, Block block) throws ExecutionException {
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

        Account accountToRecover = plugin.getAccountService().findByID(accountId);
        try {
            new AccountRecoverEvent(player, accountToRecover, accountLocation).fire();
        } catch (CancelledException e) {
            if (Permission.ACCOUNT_CREATE_PROTECTED.notOwnedBy(player))
                throw new ExecutionException(plugin, Message.NO_PERMISSION_ACCOUNT_OPEN_PROTECTED);
        }

        plugin.debugf("Recovered account #%d", accountId);
        accountToRecover.setLocation(accountLocation);
        if (!accountToRecover.getBank().equals(bank)) {
            Bank oldBank = accountToRecover.getBank();
            oldBank.removeAccount(accountToRecover);
            bank.addAccount(accountToRecover);
            plugin.getBankService().update(oldBank);
            plugin.getBankService().update(bank);
        }
        accountToRecover.updateChestTitle();
        plugin.getAccountService().update(accountToRecover);
        player.sendMessage(Message.ACCOUNT_RECOVERED.translate(plugin));
    }

}
