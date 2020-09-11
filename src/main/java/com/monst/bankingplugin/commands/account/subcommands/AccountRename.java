package com.monst.bankingplugin.commands.account.subcommands;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.account.AccountField;
import com.monst.bankingplugin.events.account.AccountConfigureEvent;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AccountRename extends AccountSubCommand {

    public AccountRename() {
        super("rename", true);
    }

    @Override
    public String getHelpMessage(CommandSender sender) {
        return hasPermission(sender, Permissions.ACCOUNT_SET) ? Messages.COMMAND_USAGE_ACCOUNT_SET : "";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        if (!p.hasPermission(Permissions.ACCOUNT_CREATE)) {
            p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_SET_NICKNAME);
            return true;
        }
        StringBuilder sb = new StringBuilder(32);
        if (args.length >= 2)
            sb.append(args[1]);
        for (int i = 2; i < args.length; i++)
            sb.append(" ").append(args[i]);
        String nickname = sb.toString();

        if (!nickname.trim().isEmpty() && !Utils.isAllowedName(nickname)) {
            plugin.debug("Name \"" + nickname + "\" is not allowed");
            p.sendMessage(Messages.NAME_NOT_ALLOWED);
            return true;
        }

        ClickType.setPlayerClickType(p, ClickType.rename(nickname));
        p.sendMessage(String.format(Messages.CLICK_ACCOUNT_CHEST, "rename"));
        return true;
    }

    public static void rename(Player executor, Account account, String value) {
        if (!(account.isTrusted(executor) || executor.hasPermission(Permissions.ACCOUNT_SET_NICKNAME_OTHER))) {
            plugin.debugf("%s does not have permission to change another player's account nickname", executor.getName());
            executor.sendMessage(Messages.NO_PERMISSION_ACCOUNT_SET_NICKNAME_OTHER);
            return;
        }

        if (value.isEmpty()) {
            plugin.debugf("%s has reset %s account nickname%s (#%d)", executor.getName(),
                    (account.isOwner(executor) ? "their" : account.getOwner().getName() + "'s"),
                    (account.isCoowner(executor) ? " (is co-owner)" : ""), account.getID());
            account.setName(account.getDefaultName());
        } else {
            plugin.debugf("%s has set their account nickname to \"%s\" (#%d)",
                    executor.getName(), value, account.getID());
            account.setName(value);
        }
        executor.sendMessage(Messages.NICKNAME_SET);
        plugin.getAccountUtils().addAccount(account, true, account.callUpdateName());
        AccountConfigureEvent e = new AccountConfigureEvent(executor, account, AccountField.NICKNAME, value);
        Bukkit.getPluginManager().callEvent(e);
    }

}
