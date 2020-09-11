package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.commands.ConfirmableSubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.bank.BankRemoveAllEvent;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Set;

public class BankRemoveall extends BankCommand.SubCommand implements ConfirmableSubCommand {

    BankRemoveall() {
        super("removeall", false);
    }

    @Override
    protected String getHelpMessage(CommandSender sender) {
        return sender.hasPermission(Permissions.BANK_REMOVEALL) ? Messages.COMMAND_USAGE_BANK_REMOVEALL : "";
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " wants to remove all banks");

        if (!sender.hasPermission(Permissions.BANK_REMOVEALL)) {
            plugin.debug(sender.getName() + " does not have permission to remove all banks");
            sender.sendMessage(Messages.NO_PERMISSION_BANK_REMOVEALL);
            return true;
        }

        Set<Bank> banks = bankUtils.getBanksCopy();

        int affectedAccounts = banks.stream().map(Bank::getAccounts).mapToInt(Collection::size).sum();
        if (sender instanceof Player && Config.confirmOnRemoveAll && !isConfirmed((Player) sender, args)) {
            sender.sendMessage(String.format(Messages.ABOUT_TO_REMOVE_BANKS, banks.size(),
                    banks.size() == 1 ? "" : "s", affectedAccounts, affectedAccounts == 1 ? "" : "s"));
            sender.sendMessage(Messages.EXECUTE_AGAIN_TO_CONFIRM);
            return true;
        }

        BankRemoveAllEvent event = new BankRemoveAllEvent(sender, banks);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Bank remove all event cancelled");
            return true;
        }

        bankUtils.removeBanks(banks, true);
        plugin.debug("Bank(s) " + Utils.map(banks, bank -> "#" + bank.getID()).toString() + " removed from the database.");
        sender.sendMessage(String.format(Messages.BANKS_REMOVED, banks.size(), banks.size() == 1 ? "" : "s", affectedAccounts, affectedAccounts == 1 ? "" : "s"));
        for (Bank bank : banks) {
            Collection<OfflinePlayer> toNotify = bank.getTrustedPlayers();
            if (sender instanceof Player)
                toNotify.remove(sender);
            Utils.message(toNotify, String.format(Messages.PLAYER_REMOVED_BANK, sender.getName(), bank.getColorizedName()));
        }
        return true;
    }

}
