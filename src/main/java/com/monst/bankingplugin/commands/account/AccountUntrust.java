package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.events.account.AccountUntrustCommandEvent;
import com.monst.bankingplugin.events.account.AccountUntrustEvent;
import com.monst.bankingplugin.lang.Messages;
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

public class AccountUntrust extends SubCommand.AccountSubCommand {

    AccountUntrust() {
        super("untrust", true);
    }

    @Override
    protected String getPermission() {
        return Permissions.ACCOUNT_TRUST;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_UNTRUST;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {

        PLUGIN.debug(sender.getName() + " wants to untrust a player from an account");

        if (!sender.hasPermission(Permissions.ACCOUNT_TRUST)) {
            sender.sendMessage(Messages.get(Message.NO_PERMISSION_ACCOUNT_UNTRUST));
            return true;
        }

        if (args.length < 2)
            return false;

        OfflinePlayer playerToUntrust = Utils.getPlayer(args[1]);
        if (playerToUntrust == null) {
            sender.sendMessage(Messages.get(Message.PLAYER_NOT_FOUND, new Replacement(Placeholder.INPUT, args[1])));
            return true;
        }

        Player p = ((Player) sender);
        AccountUntrustCommandEvent event = new AccountUntrustCommandEvent(p, args);
        event.fire();
        if (event.isCancelled()) {
            PLUGIN.debug("Account untrust command event cancelled");
            return true;
        }

        sender.sendMessage(Messages.get(Message.CLICK_ACCOUNT_UNTRUST, new Replacement(Placeholder.PLAYER, playerToUntrust::getName)));
        ClickType.setUntrustClickType(p, playerToUntrust);
        PLUGIN.debug(sender.getName() + " is untrusting " + playerToUntrust.getName() + " from an account");
        return true;
    }

    @Override
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length != 1)
            return Collections.emptyList();
        return Utils.filter(Utils.getOnlinePlayerNames(),
                name -> Utils.startsWithIgnoreCase(name, args[0]));
    }

    public static void untrust(Player executor, Account account, OfflinePlayer playerToUntrust) {
        ClickType.removeClickType(executor);

        if (!account.isOwner(executor) && !executor.hasPermission(Permissions.ACCOUNT_TRUST_OTHER)) {
            if (account.isTrusted(executor)) {
                executor.sendMessage(Messages.get(Message.MUST_BE_OWNER));
                return;
            }
            executor.sendMessage(Messages.get(Message.NO_PERMISSION_ACCOUNT_UNTRUST_OTHER));
            return;
        }

        if (!account.isCoOwner(playerToUntrust)) {
            PLUGIN.debugf("%s was not a co-owner of that account and could not be removed (#%d)",
                    playerToUntrust.getName(), account.getID());
            executor.sendMessage(Messages.get(Message.NOT_A_COOWNER,
                    new Replacement(Placeholder.PLAYER, playerToUntrust::getName)
            ));
            return;
        }

        AccountUntrustEvent event = new AccountUntrustEvent(executor, account, playerToUntrust);
        event.fire();
        if (event.isCancelled()) {
            PLUGIN.debug("Account untrust event cancelled");
            return;
        }

        PLUGIN.debugf("%s has untrusted %s from %s account (#%d)", executor.getName(),	playerToUntrust.getName(),
                (account.isOwner(executor) ? "their" : account.getOwner().getName() + "'s"), account.getID());
        executor.sendMessage(Messages.get(Message.REMOVED_COOWNER,
                new Replacement(Placeholder.PLAYER, playerToUntrust::getName)
        ));
        account.untrustPlayer(playerToUntrust);
        PLUGIN.getDatabase().removeCoOwner(account, playerToUntrust, null);
    }

}
