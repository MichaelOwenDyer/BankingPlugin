package com.monst.bankingplugin.commands.control;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.events.control.ReloadEvent;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.Permissions;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class ControlReload extends SubCommand.ControlSubCommand {

    ControlReload(BankingPlugin plugin) {
		super(plugin, "reload", false);
    }

    @Override
    protected String getPermission() {
        return Permissions.RELOAD;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_RELOAD;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " is reloading the plugin");

        if (!sender.hasPermission(Permissions.RELOAD)) {
            plugin.debug(sender.getName() + " does not have permission to reload the plugin");
            sender.sendMessage(Message.NO_PERMISSION_RELOAD.translate());
            return true;
        }

        ReloadEvent event = new ReloadEvent(sender);
        event.fire();
        if (event.isCancelled()) {
            plugin.debug("Reload event cancelled");
            return true;
        }

        plugin.reload(Callback.of(result -> {
            Set<Bank> banks = result.getBanks();
            Set<Account> accounts = result.getAccounts();
            sender.sendMessage(Message.RELOADED_PLUGIN
                    .with(Placeholder.NUMBER_OF_BANKS).as(banks.size())
                    .and(Placeholder.NUMBER_OF_ACCOUNTS).as(accounts.size())
                    .translate());
            plugin.debugf("%s has reloaded %d banks and %d accounts.", sender.getName(), banks.size(), accounts.size());
        }, error -> sender.sendMessage(Message.ERROR_OCCURRED.with(Placeholder.ERROR)
                .as("Fatal error while loading banks and accounts from the database.")
                .translate())
        ));
        return true;
    }

}
