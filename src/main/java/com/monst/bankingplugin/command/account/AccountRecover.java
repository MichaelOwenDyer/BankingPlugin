package com.monst.bankingplugin.command.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.Permission;
import com.monst.bankingplugin.command.PlayerSubCommand;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.entity.geo.location.AccountLocation;
import com.monst.bankingplugin.event.account.AccountRecoverEvent;
import com.monst.bankingplugin.exception.EventCancelledException;
import com.monst.bankingplugin.exception.CommandExecutionException;
import com.monst.bankingplugin.gui.AccountRecoveryGUI;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.command.ClickAction;
import com.monst.bankingplugin.command.Permissions;
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
        return Permissions.ACCOUNT_RECOVER;
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
        new AccountRecoveryGUI(plugin, player, account -> chest -> recover(player, account, chest)).open();
    }

    private void recover(Player player, Account accountToRecover, Block chest) throws CommandExecutionException {
        ClickAction.remove(player);
        if (plugin.getAccountService().isAccount(chest))
            throw err(Message.CHEST_ALREADY_ACCOUNT);

        InventoryHolder ih = ((Chest) chest.getState()).getInventory().getHolder();
        AccountLocation newLocation = AccountLocation.from(ih);

        Bank bank = plugin.getBankService().findContaining(newLocation);
        if (bank == null)
            throw err(Message.CHEST_NOT_IN_BANK);

        try {
            new AccountRecoverEvent(player, accountToRecover, newLocation).fire();
        } catch (EventCancelledException e) {
            if (Permissions.ACCOUNT_CREATE_PROTECTED.notOwnedBy(player))
                throw err(Message.NO_PERMISSION_ACCOUNT_OPEN_PROTECTED);
        }

        plugin.debugf("Recovered account %s", accountToRecover);
        accountToRecover.setBank(bank);
        accountToRecover.setLocation(newLocation);
        accountToRecover.updateChestTitle();
        plugin.getAccountService().update(accountToRecover);
        player.sendMessage(Message.ACCOUNT_RECOVERED.translate(plugin));
    }

}
