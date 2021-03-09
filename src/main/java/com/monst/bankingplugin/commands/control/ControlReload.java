package com.monst.bankingplugin.commands.control;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.events.control.ReloadEvent;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Collection;

public class ControlReload extends ControlCommand.SubCommand {

    ControlReload() {
        super("reload", false);
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
            sender.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_RELOAD));
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
                    sender.sendMessage(LangUtils.getMessage(Message.RELOADED_PLUGIN,
                            new Replacement(Placeholder.NUMBER_OF_BANKS, banks::size),
                            new Replacement(Placeholder.NUMBER_OF_ACCOUNTS, accounts::size)
                    ));
                    plugin.debugf("%s has reloaded %d banks and %d accounts.", sender.getName(), banks.size(), accounts.size());
                }, error -> sender.sendMessage(LangUtils.getMessage(Message.ERROR_OCCURRED,
                        new Replacement(Placeholder.ERROR, "No database access! Disabling BankingPlugin.")
                )))
        );
        return true;
    }

}
