package com.monst.bankingplugin.command;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.exception.CommandExecutionException;
import com.monst.bankingplugin.exception.EventCancelledException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Translatable;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class SubCommand {
    
    private static final Cache<UUID, Integer> PLAYER_COMMAND_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.SECONDS)
            .build();

    protected final BankingPlugin plugin;
	private final String name;
    private final boolean playerOnly;

    protected SubCommand(BankingPlugin plugin, String name) {
        this(plugin, name, false);
    }

    SubCommand(BankingPlugin plugin, String name, boolean playerOnly) {
        this.plugin = plugin;
        this.name = name.toLowerCase(Locale.US);
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
     * @throws CommandExecutionException if an exception was encountered while executing the command
     * @throws EventCancelledException if the command event was cancelled by a plugin
     */
    protected abstract void execute(CommandSender sender, String[] args) throws CommandExecutionException, EventCancelledException;

    /**
     * Tab-completes the subcommand
     * @param player Sender of the command
     * @param args Arguments of the command
     * @return A list of tab completions for the subcommand (can be empty)
     */
    protected List<String> getTabCompletions(Player player, String[] args) {
		return Collections.emptyList();
    }

    /**
     * @return the permission node required to see and execute this command.
     */
    protected Permission getPermission() {
        return Permission.none();
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
    
    protected static boolean isFirstUsage(Player sender, int commandHash) {
        if (PLAYER_COMMAND_CACHE.asMap().remove(sender.getUniqueId(), commandHash))
            return false;
        PLAYER_COMMAND_CACHE.put(sender.getUniqueId(), commandHash);
        return true;
    }
    
    @SuppressWarnings("deprecation") // TODO: Non-deprecated way to look up players by name?
    protected static OfflinePlayer getPlayer(String name) {
        OfflinePlayer player = Bukkit.getPlayer(name);
        if (player == null)
            player = Bukkit.getOfflinePlayer(name);
        return player.hasPlayedBefore() ? player : null;
    }
    
    protected static boolean containsIgnoreCase(String string, String substring) {
        int len = substring.length();
        int max = string.length() - len;
        for (int i = 0; i <= max; i++)
            if (string.regionMatches(true, i, substring, 0, len))
                return true;
        return false;
    }
    
    protected CommandExecutionException err(Translatable message) {
        return new CommandExecutionException(plugin, message);
    }

}
