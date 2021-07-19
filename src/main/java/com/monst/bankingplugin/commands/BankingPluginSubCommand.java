package com.monst.bankingplugin.commands;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.Collections;
import java.util.List;

public abstract class BankingPluginSubCommand {

    protected static final BankingPlugin PLUGIN = BankingPlugin.getInstance();

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
     * @return Whether the command syntax was correct
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
     * @return the permission node required to see and execute this command.
     */
    protected String getPermission() {
        return null;
    }

    /**
     * @return the {@link Message} describing the syntax of this subcommand.
     */
    protected abstract Message getUsageMessage();

    /**
     * Sends a message to the command sender describing how to use this subcommand
     * @param sender Sender to receive the help message
     * @return The help message for the command.
     */
    String getHelpMessage(CommandSender sender, String commandName) {
        if (hasPermission(sender, getPermission()))
            return LangUtils.getMessage(getUsageMessage(), new Replacement(Placeholder.COMMAND, commandName));
        return "";
    }

    protected boolean hasPermission(CommandSender sender, String permission) {
        if (permission == null || sender == null || permission.isEmpty() || sender.hasPermission(permission))
            return true;
        for (PermissionAttachmentInfo permInfo : sender.getEffectivePermissions())
            if (Utils.startsWithIgnoreCase(permInfo.getPermission(), permission) && permInfo.getValue())
                return true;
        return false;
    }

}
