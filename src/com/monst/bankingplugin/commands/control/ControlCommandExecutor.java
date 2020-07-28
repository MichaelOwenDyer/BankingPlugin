package com.monst.bankingplugin.commands.control;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.events.InterestEvent;
import com.monst.bankingplugin.events.ReloadEvent;
import com.monst.bankingplugin.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ControlCommandExecutor implements CommandExecutor {

	private final BankingPlugin plugin;
	
	public ControlCommandExecutor(BankingPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

		List<ControlSubCommand> subCommands = plugin.getControlCommand().getSubCommands().stream()
				.map(cmd -> (ControlSubCommand) cmd).collect(Collectors.toList());

		ControlSubCommand subCommand = null;

		for (ControlSubCommand controlSubCommand : subCommands)
			if (controlSubCommand.getName().equalsIgnoreCase(args[0])) {
				subCommand = controlSubCommand;
				break;
			}

		if (subCommand == null) {
			plugin.getLogger().severe("Null command!");
			plugin.debug("Null command! Sender: " + sender.getName() + ", command: " + command.getName() + " "
					+ String.join(" ", args));
			return false;
		}

		switch (subCommand.getName().toLowerCase()) {
			case "version":
				showVersion(sender);
				break;
			case "config":
				if (! changeConfig(sender, args))
					sender.sendMessage(subCommand.getHelpMessage(sender));
				break;
			case "reload":
				promptReload(sender);
				break;
			case "update":
				// checkUpdates(sender);
				return false;
			case "payinterest":
				promptPayout(sender);
				break;
			default:
				return false;
		}
		return true;
	}
	
	private void showVersion(CommandSender sender) {
		if (sender instanceof Player) {
			sender.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "Banking" + ChatColor.DARK_GREEN + ChatColor.BOLD + "Plugin" + ChatColor.RESET 
								  + ChatColor.AQUA + " v" + plugin.getDescription().getVersion()
								  + ChatColor.DARK_GRAY + "\n         by monst");
		} else
			sender.sendMessage(Utils.getVersionMessage());
	}

	private boolean changeConfig(CommandSender sender, String[] args) {
		plugin.debug(sender.getName() + " is adjusting the config");

		if (!sender.hasPermission(Permissions.CONFIG)) {
			plugin.debug(sender.getName() + " does not have permission to adjust the config");
			sender.sendMessage(Messages.NO_PERMISSION_CONFIG);
			return true;
		}

		if (args.length < 4)
			return false;

		String property = args[2];
		StringBuilder sb = new StringBuilder(args[3]);
		for (int i = 4; i < args.length; i++) {
			sb.append(" ").append(args[i]);
		}
		String value = sb.toString();

		switch (args[1].toLowerCase()) {
		case "set":
			plugin.getPluginConfig().set(property, value);
			sender.sendMessage(String.format(Messages.CHANGED_CONFIG_SET, property, value));
			break;
		case "add":
			plugin.getPluginConfig().add(property, value);
			sender.sendMessage(String.format(Messages.CHANGED_CONFIG_ADDED, property));
			break;
		case "remove":
			plugin.getPluginConfig().remove(property, value);
			sender.sendMessage(String.format(Messages.CHANGED_CONFIG_REMOVED, property));
			break;
		default:
			return false;
		}
		return true;
	}

	/**
	 * A given player reloads the accounts
	 * 
	 * @param sender The command executor
	 */
	private void promptReload(final CommandSender sender) {
		plugin.debug(sender.getName() + " is reloading the plugin");

		if (!sender.hasPermission(Permissions.RELOAD)) {
			plugin.debug(sender.getName() + " does not have permission to reload the plugin");
			sender.sendMessage(Messages.NO_PERMISSION_RELOAD);
			return;
		}

		ReloadEvent event = new ReloadEvent(sender);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			plugin.debug("Reload event cancelled");
			return;
		}

		plugin.getBankUtils().reload(true, true,
				new Callback<AbstractMap.SimpleEntry<Collection<Bank>, Collection<Account>>>(plugin) {
			@Override
			public void onResult(AbstractMap.SimpleEntry<Collection<Bank>, Collection<Account>> result) {
				sender.sendMessage(String.format(Messages.RELOADED_BANKS,
						result.getKey().size(), result.getValue().size()));
				plugin.debug(String.format(sender.getName() + " has reloaded %d banks and %d accounts.",
						result.getKey().size(), result.getValue().size()));
			}

			@Override
			public void onError(Throwable throwable) {
				// Database connection probably failed => disable plugin to prevent more errors
				sender.sendMessage(Messages.ERROR_OCCURRED + "No database access: Disabling BankingPlugin");
				plugin.getLogger().severe("No database access: Disabling BankingPlugin");
				if (throwable != null)
					plugin.getLogger().severe(throwable.getMessage());
				plugin.getServer().getPluginManager().disablePlugin(plugin);
			}
		});
	}

	/**
	 * A given player checks for updates
	 * 
	 * @param sender The command executor
	 */
	private void checkUpdates(CommandSender sender) {
		plugin.debug(sender.getName() + " is checking for updates");

		if (!sender.hasPermission(Permissions.UPDATE)) {
			plugin.debug(sender.getName() + " does not have permission to update the plugin");
			sender.sendMessage(Messages.NO_PERMISSION_UPDATE);
			return;
		}

		// sender.sendMessage(Messages.UPDATE_CHECKING);

		UpdateChecker uc = new UpdateChecker(BankingPlugin.getInstance());
		UpdateChecker.UpdateCheckerResult result = uc.check();

		if (result == UpdateChecker.UpdateCheckerResult.TRUE) {
			// plugin.setLatestVersion(uc.getVersion());
			// plugin.setDownloadLink(uc.getLink());
			// plugin.setUpdateNeeded(true);

			if (sender instanceof Player) {
				// Utils.sendUpdateMessage(plugin, (Player) sender);
			} else {
				// sender.sendMessage(Messages.UPDATE_AVAILABLE);
			}

		} else if (result == UpdateChecker.UpdateCheckerResult.FALSE) {
			// plugin.setLatestVersion("");
			// plugin.setDownloadLink("");
			// plugin.setUpdateNeeded(false);
			// sender.sendMessage(Messages.UPDATE_NO_UPDATE);
		} else {
			// plugin.setLatestVersion("");
			// plugin.setDownloadLink("");
			// plugin.setUpdateNeeded(false);
			// sender.sendMessage(Messages.UPDATE_ERROR);
		}
	}

	private void promptPayout(CommandSender sender) {
		plugin.debug(sender.getName() + " is triggering an interest payout");

		if (!sender.hasPermission(Permissions.PAY_INTEREST)) {
			plugin.debug(sender.getName() + " does not have permission to trigger an interest payout");
			sender.sendMessage(Messages.NO_PERMISSION_PAY_INTEREST);
			return;
		}

		sender.sendMessage(Messages.INTEREST_PAYOUT_TRIGGERED);

		InterestEvent event = new InterestEvent(plugin);
		Bukkit.getPluginManager().callEvent(event);
	}
}
