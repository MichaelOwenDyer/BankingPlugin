package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.events.account.AccountPreUntrustEvent;
import com.monst.bankingplugin.events.account.AccountUntrustEvent;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Bukkit;
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
        return sender.hasPermission(Permissions.ACCOUNT_TRUST) ? LangUtils.getMessage(Message.COMMAND_USAGE_ACCOUNT_UNTRUST, getReplacement()) : "";
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Player p = ((Player) sender);
        if (args.length < 2)
            return false;

        plugin.debug(p.getName() + " wants to untrust a player from an account");

        if (!p.hasPermission(Permissions.ACCOUNT_TRUST)) {
            p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_UNTRUST));
            return true;
        }
        OfflinePlayer playerToUntrust = Utils.getPlayer(args[1]);
        if (playerToUntrust == null) {
            p.sendMessage(LangUtils.getMessage(Message.PLAYER_NOT_FOUND, new Replacement(Placeholder.STRING, args[1])));
            return true;
        }

        AccountPreUntrustEvent event = new AccountPreUntrustEvent(p, args);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Account pre-untrust event cancelled");
            return true;
        }

        p.sendMessage(LangUtils.getMessage(Message.CLICK_ACCOUNT_UNTRUST, new Replacement(Placeholder.PLAYER, playerToUntrust::getName)));
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
                p.sendMessage(LangUtils.getMessage(Message.MUST_BE_OWNER));
                return;
            }
            p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_UNTRUST_OTHER));
            return;
        }

        boolean isSelf = Utils.samePlayer(playerToUntrust, p);
        if (!account.isCoowner(playerToUntrust)) {
            plugin.debugf("%s was not a co-owner of that account and could not be removed (#%d)",
                    playerToUntrust.getName(), account.getID());
            p.sendMessage(LangUtils.getMessage(Message.NOT_A_COOWNER,
                    new Replacement(Placeholder.PLAYER, playerToUntrust::getName)
            ));
            return;
        }

        AccountUntrustEvent event = new AccountUntrustEvent(p, account, playerToUntrust);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Account untrust event cancelled");
            return;
        }

        plugin.debugf("%s has untrusted %s from %s account (#%d)", p.getName(),	playerToUntrust.getName(),
                (account.isOwner(p) ? "their" : account.getOwner().getName() + "'s"), account.getID());
        p.sendMessage(LangUtils.getMessage(Message.REMOVED_COOWNER,
                new Replacement(Placeholder.PLAYER, playerToUntrust::getName)
        ));
        account.untrustPlayer(playerToUntrust);
    }

}
