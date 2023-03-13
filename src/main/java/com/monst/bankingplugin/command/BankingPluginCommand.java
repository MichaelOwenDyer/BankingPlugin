package com.monst.bankingplugin.command;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.event.EventCancelledException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Represents a major command for this plugin, e.g. /account. This class creates and registers each major command,
 * along with all subcommands, on startup, and also provides a basic {@link CommandExecutor} and {@link TabCompleter}
 * which delegate to the concrete implementations in the subcommand classes themselves.
 */
public abstract class BankingPluginCommand implements CommandExecutor, TabCompleter {

	protected final BankingPlugin plugin;
	private final String name;
	private final String desc;
	private final Map<String, SubCommand> subCommands;

	protected BankingPluginCommand(BankingPlugin plugin, String name, Message desc, SubCommand... subCommands) {
		this.plugin = plugin;
		this.name = name;
		this.desc = desc.translate(plugin);
		this.subCommands = Arrays.stream(subCommands)
				.collect(Collectors.toMap(SubCommand::getName, subCommand -> subCommand));
		register();
	}

	private void register() {

		PluginCommand pluginCommand;
		plugin.debug("Creating plugin command \"%s\"", name);
		try {
			Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			constructor.setAccessible(true);

			pluginCommand = constructor.newInstance(name, plugin);
			pluginCommand.setDescription(desc);
			pluginCommand.setUsage("/" + name);
			pluginCommand.setExecutor(this);
			pluginCommand.setTabCompleter(this);

		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
			plugin.log(Level.SEVERE, "Failed to create command '" + name + "'!");
			plugin.debug(e);
			return;
		}

		plugin.debug("Registering command \"%s\"", name);
		try {
			Field commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
			commandMapField.setAccessible(true);

			CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getPluginManager());
			commandMap.register(plugin.getName(), pluginCommand);
		} catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
			plugin.log(Level.SEVERE, "Failed to register command '" + name + "'!");
			plugin.debug(e);
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0)
			return true;
		SubCommand subCommand = subCommands.get(args[0]);
		if (subCommand == null) {
			plugin.debug("Sending command usage messages to " + sender.getName());
			subCommands.values().stream()
					.filter(cmd -> cmd.getPermission().ownedBy(sender))
					.map(cmd -> cmd.getUsageMessage()
							.with(Placeholder.COMMAND).as(name + " " + cmd.getName())
							.translate(plugin))
					.forEach(sender::sendMessage);
			return true;
		}
		if (subCommand.isPlayerOnly() && !(sender instanceof Player)) {
			sender.sendMessage(Message.PLAYER_COMMAND_ONLY.translate(plugin));
			return true;
		}
		if (subCommand.getPermission().notOwnedBy(sender)) {
			plugin.debug("%s does not have permission to execute command /%s %s",
					sender.getName(), name, subCommand.getName());
			sender.sendMessage(subCommand.getNoPermissionMessage().translate(plugin));
			return true;
		}
		String[] arguments = Arrays.copyOfRange(args, 1, args.length);
		plugin.debug("%s is executing command /%s %s %s",
				sender.getName(), name, subCommand.getName(), String.join(" ", arguments));
		if (arguments.length < subCommand.getMinimumArguments()) {
			plugin.debug("Too few arguments. Sending usage message to " + sender.getName());
			sender.sendMessage(subCommand.getUsageMessage()
					.with(Placeholder.COMMAND).as(name + " " + subCommand.getName())
					.translate(plugin));
			return true;
		}
		try {
			subCommand.execute(sender, arguments);
		} catch (CommandExecutionException e) {
			sender.sendMessage(e.getLocalizedMessage());
			plugin.debug("Could not execute %s's command: %s", sender.getName(), e.getMessage());
		} catch (EventCancelledException e) {
			plugin.debug("Command cancelled: " + e);
		}
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		
		if (!(sender instanceof Player))
			return Collections.emptyList();
		if (args.length == 0)
			return Collections.emptyList();
		
		if (args.length == 1)
			return subCommands.values().stream()
					.map(SubCommand::getName)
					.filter(name -> StringUtil.startsWithIgnoreCase(name, args[0]))
					.collect(Collectors.toList());
		
		String[] arguments = Arrays.copyOfRange(args, 1, args.length);
		SubCommand subCommand = subCommands.get(args[0]);
		if (subCommand == null)
			return Collections.emptyList();
		if (subCommand.getPermission().notOwnedBy(sender))
			return Collections.emptyList();
		return subCommand.getTabCompletions((Player) sender, arguments);
	}

}
