package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class AccountUntrust extends AccountCommand.SubCommand {

    AccountUntrust() {
        super("untrust", true);
    }

    @Override
    protected String getHelpMessage(CommandSender sender) {
        return sender.hasPermission(Permissions.ACCOUNT_TRUST) ? Messages.COMMAND_USAGE_ACCOUNT_UNTRUST : "";
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        if (args.length < 2)
            return false;

        plugin.debug(p.getName() + " wants to untrust a player from an account");

        if (!p.hasPermission(Permissions.ACCOUNT_TRUST)) {
            p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_UNTRUST);
            return true;
        }
        OfflinePlayer playerToUntrust = Utils.getPlayer(args[1]);
        if (playerToUntrust == null) {
            p.sendMessage(String.format(Messages.PLAYER_NOT_FOUND, args[1]));
            return true;
        }

        p.sendMessage(String.format(Messages.CLICK_ACCOUNT_CHEST,
                "remove " + (Utils.samePlayer(playerToUntrust, p) ? "yourself" : playerToUntrust.getName()) + "as a co-owner"));
        ClickType.setPlayerClickType(p, ClickType.untrust(playerToUntrust));
        plugin.debug(p.getName() + " is untrusting " + playerToUntrust.getName() + " from an account");
        return true;
    }

    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length != 2)
            return Collections.emptyList();
        return Utils.filter(Utils.getOnlinePlayerNames(plugin),
                name -> name.toLowerCase().startsWith(args[1].toLowerCase()));
    }

    public static void untrust(Player p, Account account, OfflinePlayer playerToUntrust) {
        if (!account.isOwner(p) && !p.hasPermission(Permissions.ACCOUNT_TRUST_OTHER)) {
            if (account.isTrusted(p)) {
                p.sendMessage(Messages.MUST_BE_OWNER);
                return;
            }
            p.sendMessage(Messages.NO_PERMISSION_ACCOUNT_UNTRUST_OTHER);
            return;
        }

        boolean isSelf = Utils.samePlayer(playerToUntrust, p);
        if (!account.isCoowner(playerToUntrust)) {
            plugin.debugf("%s was not a co-owner of that account and could not be removed (#%d)",
                    playerToUntrust.getName(), account.getID());
            p.sendMessage(String.format(Messages.NOT_A_COOWNER, isSelf ? "You are" : playerToUntrust.getName() + " is", "account"));
            return;
        }

        plugin.debugf("%s has untrusted %s from %s account (#%d)", p.getName(),	playerToUntrust.getName(),
                (account.isOwner(p) ? "their" : account.getOwner().getName() + "'s"), account.getID());
        p.sendMessage(String.format(Messages.REMOVED_COOWNER, isSelf ? "You were" : playerToUntrust.getName() + " was"));
        account.untrustPlayer(playerToUntrust);
    }

}
