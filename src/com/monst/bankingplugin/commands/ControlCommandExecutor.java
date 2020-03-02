package com.monst.bankingplugin.commands;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.events.bank.ReloadEvent;
import com.monst.bankingplugin.utils.BankUtils;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.UpdateChecker;

public class ControlCommandExecutor implements CommandExecutor {

	private BankingPlugin plugin;
	private BankUtils bankUtils;
	
	public ControlCommandExecutor(BankingPlugin plugin) {
		this.plugin = plugin;
		this.bankUtils = plugin.getBankUtils();
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
					+ Arrays.stream(args).collect(Collectors.joining(" ")));
			return false;
		}

		switch (subCommand.getName().toLowerCase()) {
		case "config":
			return changeConfig(sender, args);
		case "reload":
			promptReload(sender);
		case "update":
			// checkUpdates(sender);
			return false;
		default:
			return false;
		}
	}
	
	private boolean changeConfig(CommandSender sender, String[] args) {
		plugin.debug(sender.getName() + " is changing the configuration");

		String property = args[2];
		String value = args[3];

		switch (args[1].toLowerCase()) {
		case "set":
			plugin.getPluginConfig().set(property, value);
			sender.sendMessage(Messages.CHANGED_CONFIG_SET);
		case "add":
			plugin.getPluginConfig().add(property, value);
			sender.sendMessage(Messages.CHANGED_CONFIG_ADDED);
		case "remove":
			plugin.getPluginConfig().remove(property, value);
			sender.sendMessage(Messages.CHANGED_CONFIG_REMOVED);
		default:
			return false;
		}
	}

	/**
	 * A given player reloads the accounts
	 * 
	 * @param sender The command executor
	 */
	private void promptReload(final CommandSender sender) {
		plugin.debug(sender.getName() + " is reloading the accounts");

		ReloadEvent event = new ReloadEvent(sender);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			plugin.debug("Reload event cancelled");
			return;
		}

		bankUtils.reload(true, true, new Callback<Integer[]>(plugin) {
			@Override
			public void onResult(Integer[] result) {
				sender.sendMessage(
						Messages.getWithValues(Messages.RELOADED_BANKS, new Object[] { result[0], result[1] }));
				plugin.debug(sender.getName() + " has reloaded " + result[0] + " banks and " + result[1] + " accounts.");
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
}
