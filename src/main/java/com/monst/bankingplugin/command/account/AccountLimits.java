package com.monst.bankingplugin.command.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.PlayerSubCommand;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.util.Permission;
import org.bukkit.entity.Player;

public class AccountLimits extends PlayerSubCommand {

    AccountLimits(BankingPlugin plugin) {
		super(plugin, "limits");
    }

    @Override
    protected Permission getPermission() {
        return Permission.ACCOUNT_OPEN;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_LIMITS;
    }

    @Override
    protected void execute(Player player, String[] args) {
        int used = plugin.getAccountService().countByOwner(player);
        long accLimit = getPermissionLimit(player, Permission.ACCOUNT_NO_LIMIT, plugin.config().defaultAccountLimit.get());
        String limit = accLimit < 0 ? "∞" : "" + accLimit;
        plugin.debugf("%s is viewing their account limits: %s / %s", player.getName(), used, limit);
        player.sendMessage(Message.ACCOUNT_LIMIT
                .with(Placeholder.NUMBER_OF_ACCOUNTS).as(used)
                .and(Placeholder.LIMIT).as(limit)
                .translate(plugin));
    }

}
