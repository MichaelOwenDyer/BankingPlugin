package com.monst.bankingplugin.commands.control;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.commands.BankingPluginCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import org.bukkit.command.CommandSender;

public class ControlCommand extends BankingPluginCommand {

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
        this.executor = new ControlCommandExecutor(plugin);
        this.tabCompleter = new ControlTabCompleter(plugin);

		addSubCommand(new ControlSubCommand("version", false, executor, tabCompleter) {
			@Override
			public String getHelpMessage(CommandSender sender) {
				return Messages.COMMAND_USAGE_VERSION;
			}
		});

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
				return sender.hasPermission(Permissions.UPDATE) ? Messages.COMMAND_USAGE_PAY_INTEREST : "";
			}
		});

        register();
        commandCreated = true;
    }

}
