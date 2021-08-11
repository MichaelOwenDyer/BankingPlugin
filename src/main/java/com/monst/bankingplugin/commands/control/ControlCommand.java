package com.monst.bankingplugin.commands.control;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.commands.BankingPluginCommand;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.lang.Message;

import java.util.Arrays;
import java.util.List;

public class ControlCommand extends BankingPluginCommand {

	private static boolean commandCreated = false;

    public ControlCommand(BankingPlugin plugin) {
        super(plugin, Config.controlCommandName.get(), Message.CONTROL_COMMAND_DESC);
    }

    @Override
    protected List<SubCommand> getSubCommands() {
        return Arrays.asList(
                new ControlConfigure(plugin),
                new ControlPayInterest(plugin),
                new ControlReload(plugin),
                new ControlUpdate(plugin),
                new ControlVersion(plugin)
        );
    }

    @Override
    protected boolean isCreated() {
        return commandCreated;
    }

    @Override
    protected void setCreated() {
        commandCreated = true;
    }

}
