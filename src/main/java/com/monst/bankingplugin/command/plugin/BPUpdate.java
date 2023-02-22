package com.monst.bankingplugin.command.plugin;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.Permission;
import com.monst.bankingplugin.command.Permissions;
import com.monst.bankingplugin.command.SubCommand;
import com.monst.bankingplugin.gui.UpdateGUI;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BPUpdate extends SubCommand {

    BPUpdate(BankingPlugin plugin) {
		super(plugin, "update");
    }

    @Override
    protected Permission getPermission() {
        return Permissions.UPDATE;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_UPDATE;
    }

    @Override
    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION_UPDATE;
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        sender.sendMessage(Message.UPDATE_CHECKING.translate(plugin));
    
        plugin.getUpdaterService().checkForUpdate().then(update -> {
            if (update == null) {
                sender.sendMessage(Message.NO_UPDATE_AVAILABLE.translate(plugin));
                return;
            }
            
            sender.sendMessage(Message.UPDATE_AVAILABLE.with(Placeholder.VERSION).as(update.getVersion()).translate(plugin));
            
            if (sender instanceof Player)
                new UpdateGUI(plugin, (Player) sender, update).open();
            
            if (args.length > 0 && args[0].equalsIgnoreCase("download"))
                update.download();
        }).catchError(error -> sender.sendMessage(Message.UPDATE_CHECK_ERROR.translate(plugin)));
    }

}
