package com.monst.bankingplugin.commands.control;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.commands.BankingPluginCommand;
import com.monst.bankingplugin.commands.BankingPluginSubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;

import java.util.stream.Stream;

public class ControlCommand extends BankingPluginCommand<ControlCommand.SubCommand> {

	private static boolean commandCreated = false;

    public ControlCommand(final BankingPlugin plugin) {

        super(plugin);

        if (commandCreated) {
            IllegalStateException e = new IllegalStateException("Command \"" + name + "\" has already been registered!");
            plugin.debug(e);
            throw e;
        }

        this.name = Config.controlCommandName.get();
        this.desc = LangUtils.getMessage(Message.CONTROL_COMMAND_DESC);
		this.pluginCommand = super.createPluginCommand();

        Stream.of(
                new ControlConfigure(),
                new ControlPayinterest(),
                new ControlReload(),
                new ControlUpdate(),
                new ControlVersion()
        ).forEach(this::addSubCommand);

        register();
        commandCreated = true;

    }

    abstract static class SubCommand extends BankingPluginSubCommand {

        SubCommand(String name, boolean playerCommand) {
            super(name, playerCommand);
        }

    }

}
