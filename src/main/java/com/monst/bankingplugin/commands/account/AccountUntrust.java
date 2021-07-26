package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.events.account.AccountUntrustCommandEvent;
import com.monst.bankingplugin.events.account.AccountUntrustEvent;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class AccountUntrust extends SubCommand.AccountSubCommand {

    AccountUntrust(BankingPlugin plugin) {
		super(plugin, "untrust", true);
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

        plugin.debug(sender.getName() + " wants to untrust a player from an account");

        if (!sender.hasPermission(Permissions.ACCOUNT_TRUST)) {
            sender.sendMessage(Message.NO_PERMISSION_ACCOUNT_UNTRUST.translate());
            return true;
        }

        if (args.length < 2)
            return false;

        OfflinePlayer playerToUntrust = Utils.getPlayer(args[1]);
        if (playerToUntrust == null) {
            sender.sendMessage(Message.PLAYER_NOT_FOUND.with(Placeholder.INPUT).as(args[1]).translate());
            return true;
        }

        Player p = ((Player) sender);
        AccountUntrustCommandEvent event = new AccountUntrustCommandEvent(p, args);
        event.fire();
        if (event.isCancelled()) {
            plugin.debug("Account untrust command event cancelled");
            return true;
        }

        sender.sendMessage(Message.CLICK_ACCOUNT_UNTRUST.with(Placeholder.PLAYER).as(playerToUntrust.getName()).translate());
        ClickType.setUntrustClickType(p, playerToUntrust);
        plugin.debug(sender.getName() + " is untrusting " + playerToUntrust.getName() + " from an account");
        return true;
    }

    public static void untrust(BankingPlugin plugin, Player executor, Account account, OfflinePlayer playerToUntrust) {
        ClickType.removeClickType(executor);

        if (!account.isOwner(executor) && !executor.hasPermission(Permissions.ACCOUNT_TRUST_OTHER)) {
            if (account.isTrusted(executor)) {
                executor.sendMessage(Message.MUST_BE_OWNER.translate());
                return;
            }
            executor.sendMessage(Message.NO_PERMISSION_ACCOUNT_UNTRUST_OTHER.translate());
            return;
        }

        if (!account.isCoOwner(playerToUntrust)) {
            plugin.debugf("%s was not a co-owner of that account and could not be removed (#%d)",
                    playerToUntrust.getName(), account.getID());
            executor.sendMessage(Message.NOT_A_COOWNER.with(Placeholder.PLAYER).as(playerToUntrust.getName()).translate());
            return;
        }

        AccountUntrustEvent event = new AccountUntrustEvent(executor, account, playerToUntrust);
        event.fire();
        if (event.isCancelled()) {
            plugin.debug("Account untrust event cancelled");
            return;
        }

        plugin.debugf("%s has untrusted %s from account #%d", executor.getName(), playerToUntrust.getName(), account.getID());
        executor.sendMessage(Message.REMOVED_COOWNER.with(Placeholder.PLAYER).as(playerToUntrust.getName()).translate());
        account.untrustPlayer(playerToUntrust);
        plugin.getDatabase().removeCoOwner(account, playerToUntrust, null);
    }

    @Override
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length != 1)
            return Collections.emptyList();
        return Utils.filter(Utils.getOnlinePlayerNames(),
                name -> Utils.startsWithIgnoreCase(name, args[0]));
    }

}
