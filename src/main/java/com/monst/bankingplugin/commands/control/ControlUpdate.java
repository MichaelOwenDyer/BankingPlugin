package com.monst.bankingplugin.commands.control;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.utils.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ControlUpdate extends SubCommand {

    ControlUpdate(BankingPlugin plugin) {
		super(plugin, "update", false);
    }

    @Override
    protected String getPermission() {
        return Permissions.UPDATE;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_UPDATE;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " is checking for updates");

        if (!sender.hasPermission(Permissions.UPDATE)) {
            plugin.debug(sender.getName() + " does not have permission to update the plugin");
            sender.sendMessage(Message.NO_PERMISSION_UPDATE.translate());
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Sorry! That feature is not yet implemented.");
        return true;
    }

//    sender.sendMessage(Message.UPDATE_CHECKING.translate());
//
//    UpdateChecker uc = new UpdateChecker(plugin);
//        switch (uc.check()) {
//        case TRUE:
//            sender.sendMessage(Message.UPDATE_AVAILABLE.with(Placeholder.VERSION).as(uc.getVersion()).translate());
//            sender.sendMessage(Message.UPDATE_CLICK_TO_DOWNLOAD.with(Placeholder.LINK).as(uc.getLink()).translate());
//            break;
//        case FALSE:
//            sender.sendMessage(Message.UPDATE_NO_UPDATE.translate());
//        case ERROR:
//            sender.sendMessage(Message.UPDATE_ERROR.translate());
//    }
//        return true;

}
