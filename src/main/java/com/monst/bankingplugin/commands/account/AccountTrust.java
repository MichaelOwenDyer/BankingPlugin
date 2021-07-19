package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.events.account.AccountTrustCommandEvent;
import com.monst.bankingplugin.events.account.AccountTrustEvent;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
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
    protected String getPermission() {
        return Permissions.ACCOUNT_TRUST;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_TRUST;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        PLUGIN.debug(sender.getName() + " wants to trust a player to an account");

        if (!sender.hasPermission(Permissions.ACCOUNT_TRUST)) {
            sender.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_TRUST));
            return true;
        }

        if (args.length < 2)
            return false;

        OfflinePlayer playerToTrust = Utils.getPlayer(args[1]);
        if (playerToTrust == null) {
            sender.sendMessage(LangUtils.getMessage(Message.PLAYER_NOT_FOUND, new Replacement(Placeholder.INPUT, args[1])));
            return true;
        }

        Player p = ((Player) sender);
        AccountTrustCommandEvent event = new AccountTrustCommandEvent(p, args);
        event.fire();
        if (event.isCancelled()) {
            PLUGIN.debug("Account trust command event cancelled");
            return true;
        }

        sender.sendMessage(LangUtils.getMessage(Message.CLICK_ACCOUNT_TRUST, new Replacement(Placeholder.PLAYER, playerToTrust::getName)));
        ClickType.setTrustClickType(p, playerToTrust);
        PLUGIN.debug(sender.getName() + " is trusting " + playerToTrust.getName() + " to an account");
        return true;
    }

    @Override
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length != 1)
            return Collections.emptyList();
        List<String> onlinePlayers = Utils.getOnlinePlayerNames();
        if (!sender.hasPermission(Permissions.ACCOUNT_TRUST_OTHER))
            onlinePlayers.remove(sender.getName());
        return Utils.filter(onlinePlayers, name -> Utils.startsWithIgnoreCase(name, args[0]));
    }

    public static void trust(Player executor, Account account, OfflinePlayer playerToTrust) {
        ClickType.removeClickType(executor);

        if (!account.isOwner(executor) && !executor.hasPermission(Permissions.ACCOUNT_TRUST_OTHER)) {
            if (account.isTrusted(executor)) {
                executor.sendMessage(LangUtils.getMessage(Message.MUST_BE_OWNER));
                return;
            }
            executor.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_ACCOUNT_TRUST_OTHER));
            return;
        }

        if (account.isTrusted(playerToTrust)) {
            PLUGIN.debugf("%s was already trusted on that account (#%d)", playerToTrust.getName(), account.getID());
            executor.sendMessage(LangUtils.getMessage(account.isOwner(playerToTrust) ? Message.ALREADY_OWNER : Message.ALREADY_COOWNER,
                    new Replacement(Placeholder.PLAYER, playerToTrust::getName)
            ));
            return;
        }

        AccountTrustEvent event = new AccountTrustEvent(executor, account, playerToTrust);
        event.fire();
        if (event.isCancelled()) {
            PLUGIN.debug("Account trust event cancelled");
            return;
        }

        PLUGIN.debugf("%s has trusted %s to %s account (#%d)", executor.getName(), playerToTrust.getName(),
                (account.isOwner(executor) ? "their" : account.getOwner().getName() + "'s"), account.getID());
        executor.sendMessage(LangUtils.getMessage(Message.ADDED_COOWNER,
                new Replacement(Placeholder.PLAYER, playerToTrust::getName)
        ));
        account.trustPlayer(playerToTrust);
        PLUGIN.getDatabase().addCoOwner(account, playerToTrust, null);
    }

}
