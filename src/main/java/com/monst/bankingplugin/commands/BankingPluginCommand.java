package com.monst.bankingplugin.commands;

import com.monst.bankingplugin.BankingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BankingPluginCommand {

	private final BankingPlugin plugin;
	protected String name;
	protected String desc;
	protected PluginCommand pluginCommand;
	protected CommandExecutor executor;
	protected TabCompleter tabCompleter;

	private final List<BankingPluginSubCommand> subCommands = new ArrayList<>();

	protected BankingPluginCommand(final BankingPlugin plugin) {
		this.plugin = plugin;
	}

	protected PluginCommand createPluginCommand() {
		plugin.debug("Creating plugin command \"" + name + "\"");
		try {
			Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			c.setAccessible(true);

			PluginCommand cmd = c.newInstance(name, plugin);
			cmd.setDescription(desc);
			cmd.setUsage("/" + name);
			cmd.setExecutor(new BaseCommandExecutor());
			cmd.setTabCompleter(new BaseTabCompleter());

			return cmd;
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException
				| InstantiationException e) {
			plugin.getLogger().severe("Failed to create command \"" + name + "\"!");
			plugin.debug("Failed to create plugin command \"" + name + "\"!");
			plugin.debug(e);
		}

		return null;
	}

	protected void register() {
		if (pluginCommand == null)
			return;

		plugin.debug("Registering command \"" + name + "\"");

		try {
			Field f = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
			f.setAccessible(true);

			Object commandMapObject = f.get(Bukkit.getPluginManager());
			if (commandMapObject instanceof CommandMap) {
				CommandMap commandMap = (CommandMap) commandMapObject;
				commandMap.register(plugin.getName(), pluginCommand);
			}
		} catch (NoSuchFieldException | IllegalAccessException e) {
			plugin.getLogger().severe("Failed to register plugin command!");
			plugin.debug("Failed to register plugin command!");
			plugin.debug(e);
		}
	}

	/**
	 * Sends the basic help message
	 *
	 * @param sender {@link CommandSender} who will receive the message
	 */
	protected void sendBasicHelpMessage(CommandSender sender) {
		plugin.debug("Sending basic help message to " + sender.getName());
		for (BankingPluginSubCommand subCommand : subCommands) {
			String msg = subCommand.getHelpMessage(sender);
			if (msg == null || msg.isEmpty())
				continue;
			sender.sendMessage(msg);
		}
	}

	private class BaseCommandExecutor implements CommandExecutor {

		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (args.length > 0) {
				for (BankingPluginSubCommand subCommand : subCommands) {
					if (subCommand.getName().equalsIgnoreCase(args[0])) {
						if (!(sender instanceof Player) && subCommand.isPlayerCommand()) {
							sender.sendMessage(ChatColor.RED + "Only players can use this command.");
							return true;
						}
						if (!subCommand.execute(sender, command, label, args))
							sendBasicHelpMessage(sender);
						return true;
					}
				}
			}
			sendBasicHelpMessage(sender);
			return true;
		}
	}

	private class BaseTabCompleter implements TabCompleter {

		@Override
		public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

			if (args.length == 1) {
				return subCommands.stream()
						.map(BankingPluginSubCommand::getName)
						.filter(name -> name.startsWith(args[0].toLowerCase()))
						.collect(Collectors.toList());
			} else if (args.length > 1) {
				for (BankingPluginSubCommand subCmd : subCommands)
					if (subCmd.getName().equalsIgnoreCase(args[0]))
						if (sender instanceof Player)
							return subCmd.getTabCompletions(sender, command, label, args);
			}
			return Collections.emptyList();
		}
	}

	public PluginCommand getCommand() {
		return pluginCommand;
	}

	protected void addSubCommand(BankingPluginSubCommand subCommand) {
		plugin.debug("Adding " + name + " subcommand \"" + subCommand.getName() + "\"");
		subCommands.add(subCommand);
	}

	public List<? extends BankingPluginSubCommand> getSubCommands() {
		return Collections.unmodifiableList(subCommands);
	}

}
