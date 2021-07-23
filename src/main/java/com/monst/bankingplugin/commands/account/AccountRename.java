package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.banking.AccountField;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountConfigureEvent;
import com.monst.bankingplugin.lang.Messages;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class AccountRename extends SubCommand.AccountSubCommand {

    AccountRename() {
        super("rename", true);
    }

    @Override
    protected String getPermission() {
        return Permissions.ACCOUNT_RENAME;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_RENAME;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        if (!p.hasPermission(Permissions.ACCOUNT_RENAME)) {
            p.sendMessage(Messages.get(Message.NO_PERMISSION_ACCOUNT_RENAME));
            return true;
        }
        StringBuilder sb = new StringBuilder(32);
        if (args.length >= 2)
            sb.append(args[1]);
        for (int i = 2; i < args.length; i++)
            sb.append(" ").append(args[i]);
        String nickname = sb.toString();

        if (!nickname.trim().isEmpty() && !Config.nameRegex.matches(nickname)) {
            PLUGIN.debug("Name \"" + nickname + "\" is not allowed");
            p.sendMessage(Messages.get(Message.NAME_NOT_ALLOWED,
                    new Replacement(Placeholder.NAME, nickname),
                    new Replacement(Placeholder.PATTERN, Config.nameRegex)
            ));
            return true;
        }

        ClickType.setRenameClickType(p, nickname);
        p.sendMessage(Messages.get(Message.CLICK_ACCOUNT_RENAME));
        return true;
    }

    public static void rename(Player executor, Account account, String value) {
        ClickType.removeClickType(executor);
        if (!(account.isTrusted(executor) || executor.hasPermission(Permissions.ACCOUNT_RENAME_OTHER))) {
            PLUGIN.debugf("%s does not have permission to rename another player's account", executor.getName());
            executor.sendMessage(Messages.get(Message.NO_PERMISSION_ACCOUNT_RENAME_OTHER));
            return;
        }

        if (value.isEmpty()) {
            PLUGIN.debugf("%s has reset %s account nickname%s (#%d)", executor.getName(),
                    (account.isOwner(executor) ? "their" : account.getOwner().getName() + "'s"),
                    (account.isCoOwner(executor) ? " (is co-owner)" : ""), account.getID());
            account.resetName();
        } else {
            PLUGIN.debugf("%s has renamed their account to \"%s\" (#%d)",
                    executor.getName(), value, account.getID());
            account.setName(value);
        }
        executor.sendMessage(Messages.get(Message.ACCOUNT_RENAMED, new Replacement(Placeholder.ACCOUNT_NAME, account::getChestName)));
        PLUGIN.getAccountRepository().update(account, account.callUpdateChestName(), AccountField.NICKNAME);
        new AccountConfigureEvent(executor, account, AccountField.NICKNAME, value).fire();
    }

    @Override
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 1 && args[0].isEmpty())
            return Collections.singletonList("[name]");
        return Collections.emptyList();
    }

}
