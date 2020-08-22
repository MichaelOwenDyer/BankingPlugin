package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.commands.BankingPluginCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class AccountCommand extends BankingPluginCommand {

	private static boolean commandCreated = false;

	public AccountCommand(final BankingPlugin plugin) {

		super(plugin);

		if (commandCreated) {
			IllegalStateException e = new IllegalStateException(
					"Command \"" + Config.mainCommandNameAccount + "\" has already been registered!");
			plugin.debug(e);
			throw e;
		}

		this.name = Config.mainCommandNameAccount;
		this.desc = Messages.ACCOUNT_COMMAND_DESC;
		this.pluginCommand = super.createPluginCommand();
		this.executor = new AccountCommandExecutor(plugin);
		this.tabCompleter = new AccountTabCompleter(plugin);

		addSubCommand(new AccountSubCommand("create", true, executor, tabCompleter) {
			@Override
			public String getHelpMessage(CommandSender sender) {
				boolean receiveCreateMessage = sender.hasPermission(Permissions.ACCOUNT_CREATE);
				if (!receiveCreateMessage) {
					for (PermissionAttachmentInfo permInfo : sender.getEffectivePermissions()) {
						String perm = permInfo.getPermission();
						if (perm.startsWith(Permissions.ACCOUNT_CREATE) && sender.hasPermission(perm)) {
							receiveCreateMessage = true;
							break;
						}
					}
				}
				return receiveCreateMessage ? Messages.COMMAND_USAGE_ACCOUNT_CREATE : "";
			}
		});

		addSubCommand(new AccountSubCommand("remove", true, executor, tabCompleter) {
			@Override
			public String getHelpMessage(CommandSender sender) {
				return Messages.COMMAND_USAGE_ACCOUNT_REMOVE;
			}
		});

		addSubCommand(new AccountSubCommand("info", true, executor, tabCompleter) {
			@Override
			public String getHelpMessage(CommandSender sender) {
				return Messages.COMMAND_USAGE_ACCOUNT_INFO;
			}
		});

		addSubCommand(new AccountSubCommand("list", false, executor, tabCompleter) {
			@Override
			public String getHelpMessage(CommandSender sender) {
				return Messages.COMMAND_USAGE_ACCOUNT_LIST;
			}
		});

		addSubCommand(new AccountSubCommand("limits", true, executor, tabCompleter) {
			@Override
			public String getHelpMessage(CommandSender sender) {
				return Messages.COMMAND_USAGE_ACCOUNT_LIMITS;
			}
		});

		addSubCommand(new AccountSubCommand("removeall", false, executor, tabCompleter) {
			@Override
			public String getHelpMessage(CommandSender sender) {
				return Messages.COMMAND_USAGE_ACCOUNT_REMOVEALL;
			}
		});

		addSubCommand(new AccountSubCommand("set", true, executor, tabCompleter) {
			@Override
			public String getHelpMessage(CommandSender sender) {
				return Messages.COMMAND_USAGE_ACCOUNT_SET;
			}
		});

		addSubCommand(new AccountSubCommand("trust", true, executor, tabCompleter) {
			@Override
			public String getHelpMessage(CommandSender sender) {
				return sender.hasPermission(Permissions.ACCOUNT_TRUST) ? Messages.COMMAND_USAGE_ACCOUNT_TRUST : "";
			}
		});

		addSubCommand(new AccountSubCommand("untrust", true, executor, tabCompleter) {
			@Override
			public String getHelpMessage(CommandSender sender) {
				return sender.hasPermission(Permissions.ACCOUNT_TRUST) ? Messages.COMMAND_USAGE_ACCOUNT_UNTRUST : "";
			}
		});

		addSubCommand(new AccountSubCommand("migrate", true, executor, tabCompleter) {
			@Override
			public String getHelpMessage(CommandSender sender) {
				return sender.hasPermission(Permissions.ACCOUNT_CREATE) ? Messages.COMMAND_USAGE_ACCOUNT_MIGRATE : "";
			}
		});
		
		addSubCommand(new AccountSubCommand("transfer", true, executor, tabCompleter) {
			@Override
			public String getHelpMessage(CommandSender sender) {
				return sender.hasPermission(Permissions.ACCOUNT_TRANSFER) ? Messages.COMMAND_USAGE_ACCOUNT_TRANSFER : "";
			}
		});

		register();
		commandCreated = true;
	}

}
