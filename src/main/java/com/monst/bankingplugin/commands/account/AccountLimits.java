package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AccountLimits extends AccountCommand.SubCommand {

    AccountLimits() {
        super("limits", true);
    }

    @Override
    protected String getHelpMessage(CommandSender sender) {
        return hasPermission(sender, Permissions.ACCOUNT_CREATE) ? Messages.COMMAND_USAGE_ACCOUNT_LIMITS : "";
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        int used = accountUtils.getNumberOfAccounts(p);
        int accLimit = accountUtils.getAccountLimit(p);
        String limit = accLimit < 0 ? "∞" : "" + accLimit;
        plugin.debugf("%s is viewing their account limits: %s / %s", p.getName(), used, limit);
        p.sendMessage(String.format(Messages.ACCOUNT_LIMIT, used, limit));
        return true;
    }

}