package com.monst.bankingplugin.commands.control;

import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.events.control.ReloadEvent;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.Permissions;
import org.bukkit.command.CommandSender;

import java.util.Set;

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
        PLUGIN.debug(sender.getName() + " is reloading the plugin");

        if (!sender.hasPermission(Permissions.RELOAD)) {
            PLUGIN.debug(sender.getName() + " does not have permission to reload the plugin");
            sender.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_RELOAD));
            return true;
        }

        ReloadEvent event = new ReloadEvent(sender);
        event.fire();
        if (event.isCancelled()) {
            PLUGIN.debug("Reload event cancelled");
            return true;
        }

        PLUGIN.reload(Callback.of(result -> {
            Set<Bank> banks = result.getBanks();
            Set<Account> accounts = result.getAccounts();
            sender.sendMessage(LangUtils.getMessage(Message.RELOADED_PLUGIN,
                    new Replacement(Placeholder.NUMBER_OF_BANKS, banks::size),
                    new Replacement(Placeholder.NUMBER_OF_ACCOUNTS, accounts::size)
            ));
            PLUGIN.debugf("%s has reloaded %d banks and %d accounts.", sender.getName(), banks.size(), accounts.size());
        }, error ->
                sender.sendMessage(LangUtils.getMessage(Message.ERROR_OCCURRED,
                    new Replacement(Placeholder.ERROR, "Fatal error while loading banks and accounts from the database.")
                ))
        ));
        return true;
    }

}
