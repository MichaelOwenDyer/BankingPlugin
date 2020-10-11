package com.monst.bankingplugin.commands.control;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.commands.BankingPluginCommand;
import com.monst.bankingplugin.commands.BankingPluginSubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;

public class ControlCommand extends BankingPluginCommand<ControlCommand.SubCommand> {

	private static boolean commandCreated = false;

    public ControlCommand(final BankingPlugin plugin) {
    	    	
        super(plugin);
        
        if (commandCreated) {
            IllegalStateException e = new IllegalStateException("Command \"" + Config.commandNameControl + "\" has already been registered!");
            plugin.debug(e);
            throw e;
        }
        
        this.name = Config.commandNameControl;
        this.desc = LangUtils.getMessage(Message.CONTROL_COMMAND_DESC);
		this.pluginCommand = super.createPluginCommand();

        addSubCommand(new ControlConfig());
        addSubCommand(new ControlPayinterest());
		addSubCommand(new ControlReload());
		addSubCommand(new ControlUpdate());
        addSubCommand(new ControlVersion());

        register();
        commandCreated = true;

    }

    abstract static class SubCommand extends BankingPluginSubCommand {

        SubCommand(String name, boolean playerCommand) {
            super(name, playerCommand);
        }

        static Replacement getReplacement() {
            return new Replacement(Placeholder.COMMAND, Config.commandNameControl);
        }
    }
}
