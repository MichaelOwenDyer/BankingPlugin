package com.monst.bankingplugin.commands;

import org.bukkit.command.CommandSender;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;

public class ControlCommand extends GenericCommand {

	private static boolean commandCreated = false;

    public ControlCommand(final BankingPlugin plugin) {
    	    	
        super(plugin);
        
        if (commandCreated) {
            IllegalStateException e = new IllegalStateException("Command \"" + Config.mainCommandNameControl + "\" has already been registered!");
            plugin.debug(e);
            throw e;
        }
        
        this.name = Config.mainCommandNameControl;
        this.desc = Messages.COMMAND_DESC_CONTROL;
		this.pluginCommand = super.createPluginCommand();
        this.executor = new ControlCommandExecutor(plugin);

		addSubCommand(new ControlSubCommand("reload", false, executor, tabCompleter) {
            @Override
            public String getHelpMessage(CommandSender sender) {
				return sender.hasPermission(Permissions.RELOAD) ? Messages.COMMAND_USAGE_RELOAD : "";
            }
        });

		addSubCommand(new ControlSubCommand("config", false, executor, tabCompleter) {
            @Override
            public String getHelpMessage(CommandSender sender) {
				return sender.hasPermission(Permissions.CONFIG) ? Messages.COMMAND_USAGE_CONFIG : "";
            }
        });

		addSubCommand(new ControlSubCommand("update", false, executor, tabCompleter) {
            @Override
            public String getHelpMessage(CommandSender sender) {
				return sender.hasPermission(Permissions.UPDATE) ? Messages.COMMAND_USAGE_UPDATE : "";
            }
        });

		addSubCommand(new ControlSubCommand("payinterest", false, executor, tabCompleter) {
			@Override
			public String getHelpMessage(CommandSender sender) {
				return sender.hasPermission(Permissions.UPDATE) ? Messages.COMMAND_USAGE_UPDATE : "";
			}
		});

        register();
        commandCreated = true;
    }

}
