package com.monst.bankingplugin.command;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.exception.CancelledException;
import com.monst.bankingplugin.exception.ExecutionException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a major command for this plugin, e.g. /account. This class creates and registers each major command,
 * along with all subcommands, on startup, and also provides a basic {@link CommandExecutor} and {@link TabCompleter}
 * which delegate to the concrete implementations in the subcommand classes themselves.
 */
public abstract class AbstractCommand {

	protected final BankingPlugin plugin;
	private final String name;
	private final String desc;
	private final List<SubCommand> subCommands;

	protected AbstractCommand(BankingPlugin plugin, String name, Message desc, List<SubCommand> subCommands) {
		this.plugin = plugin;
		this.name = name;
		this.desc = desc.translate(plugin);
		this.subCommands = subCommands;
	}

	public void register() {

		PluginCommand pluginCommand;
		plugin.debugf("Creating plugin command \"%s\"", name);
		try {
			Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			constructor.setAccessible(true);

			pluginCommand = constructor.newInstance(name, plugin);
			pluginCommand.setDescription(desc);
			pluginCommand.setUsage("/" + name);
			pluginCommand.setExecutor(new ExecutionDelegator());
			pluginCommand.setTabCompleter(new TabCompletionDelegator());

		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
			plugin.getLogger().severe("Failed to create command \"" + name + "\"!");
			plugin.debugf("Failed to create command \"%s\"!", name);
			plugin.debug(e);
			return;
		}

		plugin.debugf("Registering command \"%s\"", name);
		try {
			Field commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
			commandMapField.setAccessible(true);

			CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getPluginManager());
			commandMap.register(plugin.getName(), pluginCommand);
		} catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
			plugin.getLogger().severe("Failed to register command \"" + name + "\"!");
			plugin.debugf("Failed to register command \"%s\"!", name);
			plugin.debug(e);
		}
	}

	/**
	 * Displays a list of all subcommands and their syntax.
	 *
	 * @param sender {@link CommandSender} who will receive the message
	 */
	private void sendCommandUsageMessage(CommandSender sender) {
		plugin.debugf("Sending basic help message to %s", sender.getName());
		for (SubCommand subCommand : subCommands) {
			if (!subCommand.hasPermission(sender))
				continue;
			sender.sendMessage(subCommand.getUsageMessage()
					.with(Placeholder.COMMAND).as(name + " " + subCommand.getName())
					.translate(plugin));
		}
	}

	private class ExecutionDelegator implements CommandExecutor {

		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (args.length > 0) {
				for (SubCommand subCommand : subCommands) {
					if (!subCommand.getName().equalsIgnoreCase(args[0]))
						continue;
					if (subCommand.isPlayerOnly() && !(sender instanceof Player)) {
						sender.sendMessage(Message.PLAYER_COMMAND_ONLY.translate(plugin));
						return true;
					}
					if (!subCommand.hasPermission(sender)) {
						plugin.debugf("%s does not have permission to execute command /%s %s",
								sender.getName(), name, subCommand.getName());
						sender.sendMessage(subCommand.getNoPermissionMessage().translate(plugin));
						return true;
					}
					String[] arguments = Arrays.copyOfRange(args, 1, args.length);
					plugin.debugf("%s is executing command /%s %s %s",
							sender.getName(), name, subCommand.getName(), String.join(" ", arguments));
					if (arguments.length < subCommand.getMinimumArguments()) {
						plugin.debugf("Too few arguments");
						sender.sendMessage(subCommand.getUsageMessage()
								.with(Placeholder.COMMAND).as(name + " " + subCommand.getName())
								.translate(plugin));
						return true;
					}
					try {
						subCommand.execute(sender, arguments);
					} catch (ExecutionException e) {
						sender.sendMessage(e.getLocalizedMessage());
						plugin.debugf("Could not execute %s's command: %s", sender.getName(), e.getMessage());
					} catch (CancelledException e) {
						plugin.debug("Command cancelled: " + e);
					}
					return true;
				}
			}
			sendCommandUsageMessage(sender);
			return true;
		}

	}

	private class TabCompletionDelegator implements TabCompleter {

		@Override
		public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

			if (!(sender instanceof Player))
				return Collections.emptyList();
			if (args.length == 0)
				return Collections.emptyList();

			if (args.length == 1)
				return subCommands.stream()
						.map(SubCommand::getName)
						.filter(name -> Utils.startsWithIgnoreCase(name, args[0]))
						.collect(Collectors.toList());

			String[] arguments = Arrays.copyOfRange(args, 1, args.length);
			for (SubCommand subCommand : subCommands)
				if (subCommand.getName().equalsIgnoreCase(args[0])) {
					if (subCommand.hasPermission(sender))
						return subCommand.getTabCompletions((Player) sender, arguments);
					break;
				}

			return Collections.emptyList();
		}

	}

}
