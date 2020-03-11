package com.monst.bankingplugin.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionAttachmentInfo;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;

public class BankCommand extends GenericCommand {

	private static boolean commandCreated = false;

    public BankCommand(final BankingPlugin plugin) {
    	    	
        super(plugin);
        
        if (commandCreated) {
            IllegalStateException e = new IllegalStateException("Command \"" + Config.mainCommandNameBank + "\" has already been registered!");
            plugin.debug(e);
            throw e;
        }
        
        this.name = Config.mainCommandNameBank;
        this.desc = Messages.COMMAND_DESC_BANK;
		this.pluginCommand = super.createPluginCommand();
        this.executor = new BankCommandExecutor(plugin);

		addSubCommand(new BankSubCommand("create", true, executor, tabCompleter) {
            @Override
            public String getHelpMessage(CommandSender sender) {
                boolean receiveCreateMessage = sender.hasPermission(Permissions.BANK_CREATE);
                if (!receiveCreateMessage) {
                    for (PermissionAttachmentInfo permInfo : sender.getEffectivePermissions()) {
                        String perm = permInfo.getPermission();
                        if (perm.startsWith(Permissions.BANK_CREATE) && sender.hasPermission(perm)) {
                            receiveCreateMessage = true;
                            break;
                        }
                    }
                }
				return receiveCreateMessage ? Messages.COMMAND_DESC_BANK_CREATE : "";
            }
        });

		addSubCommand(new BankSubCommand("remove", false, executor, tabCompleter) {
            @Override
            public String getHelpMessage(CommandSender sender) {
				return sender.hasPermission(Permissions.BANK_REMOVE) ? Messages.COMMAND_DESC_BANK_REMOVE : "";
            }
		});

		addSubCommand(new BankSubCommand("info", false, executor, tabCompleter) {
			@Override
			public String getHelpMessage(CommandSender sender) {
				return Messages.COMMAND_DESC_BANK_INFO;
			}
		});

		addSubCommand(new BankSubCommand("list", false, executor, tabCompleter) {
            @Override
            public String getHelpMessage(CommandSender sender) {
                return Messages.COMMAND_DESC_BANK_LIST;
            }
        });

        addSubCommand(new BankSubCommand("removeall", false, executor, tabCompleter) {
            @Override
            public String getHelpMessage(CommandSender sender) {
				return sender.hasPermission(Permissions.BANK_REMOVE_ALL) ? Messages.COMMAND_DESC_BANK_REMOVEALL : "";
            }
        });

        register();
        commandCreated = true;
    }

}
