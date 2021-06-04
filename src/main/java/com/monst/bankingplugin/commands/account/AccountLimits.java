package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AccountLimits extends AccountCommand.SubCommand {

    AccountLimits() {
        super("limits", true);
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
        int used = accountRepo.getOwnedBy(p).size();
        int accLimit = Utils.getAccountLimit(p);
        String limit = accLimit < 0 ? "∞" : "" + accLimit;
        PLUGIN.debugf("%s is viewing their account limits: %s / %s", p.getName(), used, limit);
        p.sendMessage(LangUtils.getMessage(Message.ACCOUNT_LIMIT,
                new Replacement(Placeholder.NUMBER_OF_ACCOUNTS, used),
                new Replacement(Placeholder.LIMIT, limit)
        ));
        return true;
    }

}
