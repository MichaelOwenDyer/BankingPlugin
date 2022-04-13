package com.monst.bankingplugin.command.plugin;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.SubCommand;
import com.monst.bankingplugin.event.control.ReloadEvent;
import com.monst.bankingplugin.exception.CancelledException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.util.Permission;
import org.bukkit.command.CommandSender;

public class BPReload extends SubCommand {

    BPReload(BankingPlugin plugin) {
		super(plugin, "reload");
    }

    @Override
    protected Permission getPermission() {
        return Permission.RELOAD;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_RELOAD;
    }

    @Override
    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION_RELOAD;
    }

    @Override
    protected void execute(CommandSender sender, String[] args) throws CancelledException {
        new ReloadEvent(sender).fire();
        plugin.reload();

        int numberOfBanks = plugin.getBankService().count();
        int numberOfAccounts = plugin.getAccountService().count();
        sender.sendMessage(Message.RELOADED_PLUGIN
                .with(Placeholder.NUMBER_OF_BANKS).as(numberOfBanks)
                .and(Placeholder.NUMBER_OF_ACCOUNTS).as(numberOfAccounts)
                .translate(plugin));
        plugin.debugf("%s has reloaded %d banks and %d accounts.", sender.getName(), numberOfBanks, numberOfAccounts);
    }

}
