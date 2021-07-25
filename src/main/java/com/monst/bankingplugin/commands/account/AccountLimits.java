package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AccountLimits extends SubCommand.AccountSubCommand {

    AccountLimits(BankingPlugin plugin) {
		super(plugin, "limits", true);
    }

    @Override
    protected String getPermission() {
        return Permissions.ACCOUNT_CREATE;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_LIMITS;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        int used = plugin.getAccountRepository().getOwnedBy(p).size();
        int accLimit = Utils.getAccountLimit(p);
        String limit = accLimit < 0 ? "∞" : "" + accLimit;
        plugin.debugf("%s is viewing their account limits: %s / %s", p.getName(), used, limit);
        p.sendMessage(Message.ACCOUNT_LIMIT
                .with(Placeholder.NUMBER_OF_ACCOUNTS).as(used)
                .and(Placeholder.LIMIT).as(limit)
                .translate());
        return true;
    }

}
