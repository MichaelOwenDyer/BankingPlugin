package com.monst.bankingplugin.command.plugin;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.SubCommand;
import com.monst.bankingplugin.gui.UpdateGUI;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.util.Callback;
import com.monst.bankingplugin.util.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BPUpdate extends SubCommand {

    BPUpdate(BankingPlugin plugin) {
		super(plugin, "update");
    }

    @Override
    protected Permission getPermission() {
        return Permission.UPDATE;
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

        plugin.checkForUpdates(Callback.of(plugin,
                updatePackage -> {
                    if (updatePackage == null) {
                        sender.sendMessage(Message.NO_UPDATE_AVAILABLE.translate(plugin));
                        return;
                    }
                    boolean download = args.length > 0 && args[0].equalsIgnoreCase("download");
                    if (sender instanceof Player) {
                        new UpdateGUI(plugin, updatePackage).open((Player) sender);
                        if (plugin.config().downloadUpdatesAutomatically.get() || download)
                            updatePackage.download(new Callback<>(plugin));
                    } else {
                        sender.sendMessage(Message.UPDATE_AVAILABLE.with(Placeholder.VERSION).as(updatePackage.getVersion()).translate(plugin));
                        if (plugin.config().downloadUpdatesAutomatically.get() || download) {
                            sender.sendMessage(Message.UPDATE_DOWNLOADING.translate(plugin));
                            updatePackage.download(Callback.of(plugin,
                                    state -> {
                                        switch (state) {
                                            case VALIDATING:
                                                sender.sendMessage(Message.UPDATE_VALIDATING.translate(plugin));
                                                break;
                                            case COMPLETED:
                                                sender.sendMessage(Message.UPDATE_DOWNLOAD_COMPLETE.translate(plugin));
                                                break;
                                        }
                                    },
                                    error -> sender.sendMessage(Message.UPDATE_DOWNLOAD_FAILED.translate(plugin))
                            ));
                        }
                    }
                },
                error -> sender.sendMessage(Message.UPDATE_CHECK_ERROR.translate(plugin))
        ));
    }


}
