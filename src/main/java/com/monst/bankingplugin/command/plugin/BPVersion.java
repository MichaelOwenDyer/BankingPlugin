package com.monst.bankingplugin.command.plugin;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.SubCommand;
import com.monst.bankingplugin.lang.ColorStringBuilder;
import com.monst.bankingplugin.lang.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BPVersion extends SubCommand {

    private final String PLAYER_MESSAGE = new ColorStringBuilder()
            .green().bold("Banking").darkGreen().bold("Plugin").aqua(" v", plugin.getDescription().getVersion())
            .darkGray("\n         by monst").toString();

    BPVersion(BankingPlugin plugin) {
		super(plugin, "version");
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_VERSION;
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player)
            sender.sendMessage(PLAYER_MESSAGE);
        else
            plugin.printStartupMessage();
    }

}
