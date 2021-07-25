package com.monst.bankingplugin.commands.control;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.UpdateChecker;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ControlUpdate extends SubCommand.ControlSubCommand {

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
