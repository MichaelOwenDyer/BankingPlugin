package com.monst.bankingplugin.commands;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Messages;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a major command for this plugin, e.g. /account. This class creates and registers each major command,
 * along with all subcommands, on startup, and also provides a basic {@link CommandExecutor} and {@link TabCompleter}
 * which delegate to the concrete implementations in the subcommand classes themselves.
 */
public abstract class BankingPluginCommand<SC extends SubCommand> {

	protected final BankingPlugin plugin;

	private final String name;
	private final String desc;
	private final PluginCommand pluginCommand;
	private final List<SC> subCommands;

	protected BankingPluginCommand(BankingPlugin plugin, String name, Message desc) throws IllegalStateException {
		if (isCreated()) {
			IllegalStateException e = new IllegalStateException("Command \"" + name + "\" has already been registered!");
			plugin.debug(e);
			throw e;
		}
		this.plugin = plugin;
		this.name = name;
		this.desc = Messages.get(desc);
		this.subCommands = new ArrayList<>();
		this.pluginCommand = createPluginCommand();
		getSubCommands().forEach(this::addSubCommand);
		register();
		setCreated();
	}

	protected abstract Stream<SC> getSubCommands();

	protected abstract boolean isCreated();

	protected abstract void setCreated();

	private PluginCommand createPluginCommand() {
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
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
			plugin.getLogger().severe("Failed to create command \"" + name + "\"!");
			plugin.debug("Failed to create command \"" + name + "\"!");
			plugin.debug(e);
		}
		return null;
	}

	private void register() {
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
			plugin.getLogger().severe("Failed to register command \"" + name + "\"!");
			plugin.debug("Failed to register command \"" + name + "\"!");
			plugin.debug(e);
		}
	}

	/**
	 * Displays a list of all subcommands and their syntax.
	 *
	 * @param sender {@link CommandSender} who will receive the message
	 */
	private void sendBasicHelpMessage(CommandSender sender) {
		plugin.debug("Sending basic help message to " + sender.getName());
		for (SC subCommand : subCommands) {
			String msg = subCommand.getHelpMessage(sender, name);
			if (msg != null && !msg.isEmpty())
				sender.sendMessage(msg);
		}
	}

	private class BaseCommandExecutor implements CommandExecutor {

		@Override
		public boolean onCommand(@Nonnull CommandSender sender, @Nonnull org.bukkit.command.Command command, @Nonnull String label, String[] args) {
			if (args.length > 0) {
				for (SC subCommand : subCommands) {
					if (subCommand.getName().equalsIgnoreCase(args[0])) {
						if (subCommand.isPlayerCommand() && !(sender instanceof Player)) {
							sender.sendMessage(ChatColor.RED + "Only players can use this command.");
							return true;
						}
						if (!subCommand.execute(sender, args)) {
							String helpMessage = subCommand.getHelpMessage(sender, name);
							if (helpMessage != null && !helpMessage.isEmpty())
								sender.sendMessage(helpMessage);
						}
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
		public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull org.bukkit.command.Command command, @Nonnull String label, @Nonnull String[] args) {

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
			for (SC subCommand : subCommands)
				if (subCommand.getName().equalsIgnoreCase(subCommandName))
					return subCommand.getTabCompletions(sender, arguments);

			return Collections.emptyList();
		}
	}

	private void addSubCommand(SC subCommand) {
		plugin.debug("Adding " + name + " subcommand \"" + subCommand.getName() + "\"");
		subCommands.add(subCommand);
	}

}
