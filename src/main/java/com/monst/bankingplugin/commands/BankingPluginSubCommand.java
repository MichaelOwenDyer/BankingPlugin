package com.monst.bankingplugin.commands;

import com.monst.bankingplugin.BankingPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.Collections;
import java.util.List;

public abstract class BankingPluginSubCommand {

    protected static final BankingPlugin plugin = BankingPlugin.getInstance();
	
	private final String name;
	private final boolean playerCommand;

    protected BankingPluginSubCommand(String name, boolean playerCommand) {
    	this.name = name;
        this.playerCommand = playerCommand;
    }

    String getName() {
        return name;
    }

    /**
     * @return Whether the command can only be used by players, not by the console
     */
    boolean isPlayerCommand() {
        return playerCommand;
    }

    /**
     * Execute the sub command
     * @param sender Sender of the command
     * @param args Arguments of the command ({@code args[0]} is the sub command's name)
     * @return Whether the sender should be sent the help message
     */
    protected abstract boolean execute(CommandSender sender, String[] args);

    /**
     * @param sender Sender of the command
     * @param args Arguments of the command ({@code args[0]} is the sub command's name)
     * @return A list of tab completions for the sub command (may be an empty list)
     */
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
		return Collections.emptyList();
    }

    /**
     * Sends a message to the command sender describing how to use this subcommand
     * @param sender Sender to receive the help message
     * @return The help message for the command.
     */
    protected abstract String getHelpMessage(CommandSender sender);

    protected boolean hasPermission(CommandSender sender, String permission) {
        boolean receiveCreateMessage = sender.hasPermission(permission);
        if (!receiveCreateMessage) {
            for (PermissionAttachmentInfo permInfo : sender.getEffectivePermissions()) {
                String perm = permInfo.getPermission();
                if (perm.startsWith(permission) && sender.hasPermission(perm)) {
                    receiveCreateMessage = true;
                    break;
                }
            }
        }
        return receiveCreateMessage;
    }

}
