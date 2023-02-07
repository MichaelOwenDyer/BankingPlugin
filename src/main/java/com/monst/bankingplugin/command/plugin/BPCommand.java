package com.monst.bankingplugin.command.plugin;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.BankingPluginCommand;
import com.monst.bankingplugin.lang.Message;

public class BPCommand extends BankingPluginCommand {

    public BPCommand(BankingPlugin plugin) {
        super(plugin, plugin.config().pluginCommandName.get(), Message.CONTROL_COMMAND_DESC,
                new BPConfigure(plugin),
                new BPPayInterest(plugin),
                new BPReload(plugin),
                new BPUpdate(plugin),
                new BPVersion(plugin),
                new BPDonate(plugin)
        );
    }

}
