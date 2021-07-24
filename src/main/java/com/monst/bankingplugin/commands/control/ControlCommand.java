package com.monst.bankingplugin.commands.control;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.commands.BankingPluginCommand;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.lang.Message;

import java.util.stream.Stream;

public class ControlCommand extends BankingPluginCommand<SubCommand.ControlSubCommand> {

	private static boolean commandCreated = false;

    public ControlCommand(final BankingPlugin plugin) {
        super(plugin, Config.controlCommandName.get(), Message.CONTROL_COMMAND_DESC);
    }

    @Override
    protected Stream<SubCommand.ControlSubCommand> getSubCommands() {
        return Stream.of(
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
