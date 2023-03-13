package com.monst.bankingplugin.command.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.Permission;
import com.monst.bankingplugin.command.Permissions;
import com.monst.bankingplugin.command.PlayerSubCommand;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import org.bukkit.entity.Player;

public class AccountLimits extends PlayerSubCommand {

    AccountLimits(BankingPlugin plugin) {
		super(plugin, "limits");
    }

    @Override
    protected Permission getPermission() {
        return Permissions.ACCOUNT_OPEN;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_LIMITS;
    }

    @Override
    protected void execute(Player player, String[] args) {
        int used = plugin.getAccountService().countByOwner(player);
        String limit = getAccountLimit(player).map(String::valueOf).orElse("∞");
        plugin.debug("%s is viewing their account limits: %s / %s", player.getName(), used, limit);
        player.sendMessage(Message.ACCOUNT_LIMIT
                .with(Placeholder.NUMBER_OF_ACCOUNTS).as(used)
                .and(Placeholder.LIMIT).as(limit)
                .translate(plugin));
    }

}
