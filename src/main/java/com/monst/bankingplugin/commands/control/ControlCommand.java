package com.monst.bankingplugin.commands.control;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.commands.BankingPluginCommand;
import com.monst.bankingplugin.commands.control.subcommands.*;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.utils.Messages;

public class ControlCommand extends BankingPluginCommand<ControlSubCommand> {

	private static boolean commandCreated = false;

    public ControlCommand(final BankingPlugin plugin) {
    	    	
        super(plugin);
        
        if (commandCreated) {
            IllegalStateException e = new IllegalStateException("Command \"" + Config.mainCommandNameControl + "\" has already been registered!");
            plugin.debug(e);
            throw e;
        }
        
        this.name = Config.mainCommandNameControl;
        this.desc = Messages.CONTROL_COMMAND_DESC;
		this.pluginCommand = super.createPluginCommand();

		addSubCommand(new ControlVersion());
		addSubCommand(new ControlReload());
		addSubCommand(new ControlConfig());
		addSubCommand(new ControlUpdate());
		addSubCommand(new ControlPayinterest());

        register();
        commandCreated = true;
    }

}
