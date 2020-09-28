package com.monst.bankingplugin.commands.control;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.events.control.ReloadEvent;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Collection;

public class ControlReload extends ControlCommand.SubCommand {

    ControlReload() {
        super("reload", false);
    }

    @Override
    protected String getHelpMessage(CommandSender sender) {
        return sender.hasPermission(Permissions.RELOAD) ? Messages.COMMAND_USAGE_RELOAD : "";
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " is reloading the plugin");

        if (!sender.hasPermission(Permissions.RELOAD)) {
            plugin.debug(sender.getName() + " does not have permission to reload the plugin");
            sender.sendMessage(Messages.NO_PERMISSION_RELOAD);
            return true;
        }

        ReloadEvent event = new ReloadEvent(plugin, sender);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Reload event cancelled");
            return true;
        }

        plugin.reload(true, true,
                Callback.of(plugin, result -> {
                    Collection<Bank> banks = result.getBanks();
                    Collection<Account> accounts = result.getAccounts();
                    plugin.getScheduler().scheduleAll();
                    sender.sendMessage(String.format(Messages.RELOADED_PLUGIN, banks.size(), accounts.size()));
                    plugin.debugf("%s has reloaded %d banks and %d accounts.", sender.getName(), banks.size(), accounts.size());
                }, error -> {
                    // Database connection probably failed => disable plugin to prevent more errors
                    sender.sendMessage(Messages.ERROR_OCCURRED + "No database access! Disabling BankingPlugin.");
                    plugin.getLogger().severe("No database access! Disabling BankingPlugin.");
                    if (error != null)
                        plugin.getLogger().severe(error.getMessage());
                    plugin.getServer().getPluginManager().disablePlugin(plugin);
                })
        );
        return true;
    }

}
