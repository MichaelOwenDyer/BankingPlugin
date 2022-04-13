package com.monst.bankingplugin.command;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.exception.CancelledException;
import com.monst.bankingplugin.exception.ExecutionException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.util.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public abstract class SubCommand {

    protected final BankingPlugin plugin;
	private final String name;
    private final boolean playerOnly;

    protected SubCommand(BankingPlugin plugin, String name) {
        this(plugin, name, false);
    }

    SubCommand(BankingPlugin plugin, String name, boolean playerOnly) {
        this.plugin = plugin;
        this.name = name;
        this.playerOnly = playerOnly;
    }

    String getName() {
        return name;
    }

    /**
     * @return Whether the command can only be used by players, not by the console
     */
    boolean isPlayerOnly() {
        return playerOnly;
    }

    /**
     * Executes the subcommand, if the sender has permission.
     * @param sender Sender of the command, can be a player or the console
     * @param args Arguments of the command, excluding the subcommand name
     * @throws ExecutionException if an exception was encountered while executing the command
     * @throws CancelledException if the command event was cancelled by a plugin
     */
    protected abstract void execute(CommandSender sender, String[] args) throws ExecutionException, CancelledException;

    /**
     * Tab-completes the subcommand
     * @param player Sender of the command
     * @param args Arguments of the command
     * @return A list of tab completions for the subcommand (can be empty)
     */
    protected List<String> getTabCompletions(Player player, String[] args) {
		return Collections.emptyList();
    }

    protected boolean hasPermission(CommandSender sender) {
        return getPermission().ownedBy(sender);
    }

    /**
     * @return the permission node required to see and execute this command.
     */
    protected Permission getPermission() {
        return Permission.NONE;
    }

    /**
     * @return the {@link Message} describing the syntax of this subcommand.
     */
    protected abstract Message getUsageMessage();

    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION;
    }

    protected int getMinimumArguments() {
        return 0;
    }

}
