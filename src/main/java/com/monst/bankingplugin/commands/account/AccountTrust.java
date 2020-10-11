package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.events.account.AccountPreTrustEvent;
import com.monst.bankingplugin.events.account.AccountTrustEvent;
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

public class AccountTrust extends AccountCommand.SubCommand {

    AccountTrust() {
        super("trust", true);
    }

    @Override
    protected String getHelpMessage(CommandSender sender) {
        return sender.hasPermission(Permissions.ACCOUNT_TRUST) ? LangUtils.getMessage(Message.COMMAND_USAGE_ACCOUNT_TRUST, getReplacement()) : "";
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " wants to trust a player to an account");

        if (!sender.hasPermission(Permissions.ACCOUNT_TRUST)) {
            sender.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_TRUST));
            return true;
        }

        if (args.length < 2)
            return false;

        OfflinePlayer playerToTrust = Utils.getPlayer(args[1]);
        if (playerToTrust == null) {
            sender.sendMessage(LangUtils.getMessage(Message.PLAYER_NOT_FOUND, new Replacement(Placeholder.STRING, args[1])));
            return true;
        }

        Player p = ((Player) sender);
        AccountPreTrustEvent event = new AccountPreTrustEvent(p, args);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Account pre-trust event cancelled");
            return true;
        }

        sender.sendMessage(LangUtils.getMessage(Message.CLICK_ACCOUNT_TRUST, new Replacement(Placeholder.PLAYER, playerToTrust::getName)));
        ClickType.setPlayerClickType(p, ClickType.trust(playerToTrust));
        plugin.debug(sender.getName() + " is trusting " + playerToTrust.getName() + " to an account");
        return true;
    }

    @Override
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length != 2)
            return Collections.emptyList();
        List<String> onlinePlayers = Utils.getOnlinePlayerNames(plugin);
        if (!sender.hasPermission(Permissions.ACCOUNT_TRUST_OTHER))
            onlinePlayers.remove(sender.getName());
        return Utils.filter(onlinePlayers, name -> name.toLowerCase().startsWith(args[1].toLowerCase()));
    }

    public static void trust(Player p, Account account, OfflinePlayer playerToTrust) {
        if (!account.isOwner(p) && !p.hasPermission(Permissions.ACCOUNT_TRUST_OTHER)) {
            if (account.isTrusted(p)) {
                p.sendMessage(LangUtils.getMessage(Message.MUST_BE_OWNER));
                return;
            }
            p.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_TRUST_OTHER));
            return;
        }

        boolean isSelf = Utils.samePlayer(playerToTrust, p);
        if (account.isTrusted(playerToTrust)) {
            plugin.debugf("%s was already trusted on that account (#%d)", playerToTrust.getName(), account.getID());
            p.sendMessage(LangUtils.getMessage(account.isOwner(playerToTrust) ? Message.ALREADY_OWNER : Message.ALREADY_COOWNER,
                    new Replacement(Placeholder.PLAYER, playerToTrust::getName)
            ));
            return;
        }

        AccountTrustEvent event = new AccountTrustEvent(p, account, playerToTrust);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Account trust event cancelled");
            return;
        }

        plugin.debugf("%s has trusted %s to %s account (#%d)", p.getName(), playerToTrust.getName(),
                (account.isOwner(p) ? "their" : account.getOwner().getName() + "'s"), account.getID());
        p.sendMessage(LangUtils.getMessage(Message.ADDED_COOWNER,
                new Replacement(Placeholder.PLAYER, playerToTrust::getName)
        ));
        account.trustPlayer(playerToTrust);
    }

}
