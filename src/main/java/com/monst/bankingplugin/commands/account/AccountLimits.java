package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AccountLimits extends AccountCommand.SubCommand {

    AccountLimits() {
        super("limits", true);
    }

    @Override
    protected String getHelpMessage(CommandSender sender) {
        return hasPermission(sender, Permissions.ACCOUNT_CREATE) ? LangUtils.getMessage(Message.COMMAND_USAGE_ACCOUNT_LIMITS, getReplacement()) : "";
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        int used = accountUtils.getNumberOfAccounts(p);
        int accLimit = accountUtils.getAccountLimit(p);
        String limit = accLimit < 0 ? "∞" : "" + accLimit;
        plugin.debugf("%s is viewing their account limits: %s / %s", p.getName(), used, limit);
        p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_LIMIT,
                new Replacement(Placeholder.NUMBER_OF_ACCOUNTS, used),
                new Replacement(Placeholder.LIMIT, limit)
        ));
        return true;
    }

}
