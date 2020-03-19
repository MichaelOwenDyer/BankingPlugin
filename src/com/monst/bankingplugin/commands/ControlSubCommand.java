package com.monst.bankingplugin.commands;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public abstract class ControlSubCommand extends GenericSubCommand {

    public ControlSubCommand(String name, boolean playerCommand, CommandExecutor executor, TabCompleter tabCompleter) {
        super(name, playerCommand, executor, tabCompleter);
    }

    /**
     * @param sender Sender to receive the help message
     * @return The help message for the command.
     */
	@Override
	public abstract String getHelpMessage(CommandSender sender);
}
