package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.commands.BankingPluginSubCommand;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public abstract class BankSubCommand extends BankingPluginSubCommand {
    
    public BankSubCommand(String name, boolean playerCommand, CommandExecutor executor, TabCompleter tabCompleter) {
        super(name, playerCommand, executor, tabCompleter);
    }


    /**
     * @param sender Sender to receive the help message
     * @return The help message for the command.
     */
	@Override
	public abstract String getHelpMessage(CommandSender sender);
}
