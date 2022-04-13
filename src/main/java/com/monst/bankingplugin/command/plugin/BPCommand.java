package com.monst.bankingplugin.command.plugin;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.AbstractCommand;
import com.monst.bankingplugin.lang.Message;

import java.util.Arrays;

public class BPCommand extends AbstractCommand {

    public BPCommand(BankingPlugin plugin) {
        super(plugin, plugin.config().pluginCommandName.get(), Message.CONTROL_COMMAND_DESC,
                Arrays.asList(
                        new BPConfigure(plugin),
                        new BPPayInterest(plugin),
                        new BPReload(plugin),
                        new BPUpdate(plugin),
                        new BPVersion(plugin),
                        new BPDonate(plugin)
                )
        );
    }

}
