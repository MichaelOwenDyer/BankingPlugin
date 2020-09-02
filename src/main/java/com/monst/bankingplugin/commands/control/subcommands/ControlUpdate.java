package com.monst.bankingplugin.commands.control.subcommands;

import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.UpdateChecker;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ControlUpdate extends ControlSubCommand {

    public ControlUpdate() {
        super("update", false);
    }

    @Override
    public String getHelpMessage(CommandSender sender) {
        return sender.hasPermission(Permissions.UPDATE) ? Messages.COMMAND_USAGE_UPDATE : "";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " is checking for updates");

        if (!sender.hasPermission(Permissions.UPDATE)) {
            plugin.debug(sender.getName() + " does not have permission to update the plugin");
            sender.sendMessage(Messages.NO_PERMISSION_UPDATE);
            return true;
        }

        // sender.sendMessage(Messages.UPDATE_CHECKING);

        UpdateChecker uc = new UpdateChecker(plugin);
        UpdateChecker.Result result = uc.check();

        if (result == UpdateChecker.Result.TRUE) {
            // plugin.setLatestVersion(uc.getVersion());
            // plugin.setDownloadLink(uc.getLink());
            // plugin.setUpdateNeeded(true);

            if (sender instanceof Player) {
                // Utils.sendUpdateMessage(plugin, (Player) sender);
            } else {
                // sender.sendMessage(Messages.UPDATE_AVAILABLE);
            }

        } else if (result == UpdateChecker.Result.FALSE) {
            // plugin.setLatestVersion("");
            // plugin.setDownloadLink("");
            // plugin.setUpdateNeeded(false);
            // sender.sendMessage(Messages.UPDATE_NO_UPDATE);
        } else {
            // plugin.setLatestVersion("");
            // plugin.setDownloadLink("");
            // plugin.setUpdateNeeded(false);
            // sender.sendMessage(Messages.UPDATE_ERROR);
        }
        return true;
    }

}
