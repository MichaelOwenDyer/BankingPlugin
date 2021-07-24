package com.monst.bankingplugin.commands.control;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.lang.Message;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ControlVersion extends SubCommand.ControlSubCommand {

    ControlVersion(BankingPlugin plugin) {
		super(plugin, "version", false);
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_VERSION;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            sender.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "Banking" + ChatColor.DARK_GREEN + ChatColor.BOLD + "Plugin" + ChatColor.RESET
                    + ChatColor.AQUA + " v" + plugin.getDescription().getVersion()
                    + ChatColor.DARK_GRAY + "\n         by monst");
        } else
            sender.sendMessage(plugin.STARTUP_MESSAGE);
        return true;
    }

}
