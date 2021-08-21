package com.monst.bankingplugin.commands;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
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
public abstract class BankingPluginCommand {

	protected final BankingPlugin plugin;
	private final String name;
	private final String desc;
	private final List<SubCommand> subCommands;

	protected BankingPluginCommand(BankingPlugin plugin, String name, Message desc) throws IllegalStateException {
		if (isCreated()) {
			IllegalStateException e = new IllegalStateException("Command \"" + name + "\" has already been registered!");
			plugin.debug(e);
			throw e;
		}
		this.plugin = plugin;
		this.name = name;
		this.desc = desc.translate();
		this.subCommands = getSubCommands();
		register();
		setCreated();
	}

	protected abstract List<SubCommand> getSubCommands();

	protected abstract boolean isCreated();

	protected abstract void setCreated();

	private void register() {

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
			String msg = subCommand.getUsageMessage(sender, name);
			if (msg != null && !msg.isEmpty())
				sender.sendMessage(msg);
		}
	}

	private class ExecutionDelegator implements CommandExecutor {

		@Override
		public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, String[] args) {
			if (args.length > 0) {
				for (SubCommand subCommand : subCommands) {
					if (!subCommand.getName().equalsIgnoreCase(args[0]))
						continue;
					if (subCommand.isPlayerCommand() && !(sender instanceof Player)) {
						sender.sendMessage(Message.PLAYER_COMMAND_ONLY.translate());
						return true;
					}
					if (!subCommand.execute(sender, args)) {
						String usageMessage = subCommand.getUsageMessage(sender, name);
						if (usageMessage != null && !usageMessage.isEmpty())
							sender.sendMessage(usageMessage);
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
		public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {

			if (!(sender instanceof Player))
				return Collections.emptyList();
			if (args.length == 0)
				return Collections.emptyList();

			String subCommandName = args[0];
			if (args.length == 1)
				return subCommands.stream()
						.map(SubCommand::getName)
						.filter(name -> Utils.startsWithIgnoreCase(name, subCommandName))
						.collect(Collectors.toList());

			String[] arguments = Arrays.copyOfRange(args, 1, args.length);
			for (SubCommand subCommand : subCommands)
				if (subCommand.getName().equalsIgnoreCase(subCommandName))
					return subCommand.getTabCompletions((Player) sender, arguments);

			return Collections.emptyList();
		}

	}

}
