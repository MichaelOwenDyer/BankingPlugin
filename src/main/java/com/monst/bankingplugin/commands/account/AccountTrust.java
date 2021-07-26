package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.events.account.AccountTrustCommandEvent;
import com.monst.bankingplugin.events.account.AccountTrustEvent;
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

public class AccountTrust extends SubCommand.AccountSubCommand {

    AccountTrust(BankingPlugin plugin) {
		super(plugin, "trust", true);
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
        plugin.debug(sender.getName() + " wants to trust a player to an account");

        if (!sender.hasPermission(Permissions.ACCOUNT_TRUST)) {
            sender.sendMessage(Message.NO_PERMISSION_ACCOUNT_TRUST.translate());
            return true;
        }

        if (args.length < 2)
            return false;

        OfflinePlayer playerToTrust = Utils.getPlayer(args[1]);
        if (playerToTrust == null) {
            sender.sendMessage(Message.PLAYER_NOT_FOUND.with(Placeholder.INPUT).as(args[1]).translate());
            return true;
        }

        Player p = ((Player) sender);
        AccountTrustCommandEvent event = new AccountTrustCommandEvent(p, args);
        event.fire();
        if (event.isCancelled()) {
            plugin.debug("Account trust command event cancelled");
            return true;
        }

        sender.sendMessage(Message.CLICK_ACCOUNT_TRUST.with(Placeholder.PLAYER).as(playerToTrust.getName()).translate());
        ClickType.setTrustClickType(p, playerToTrust);
        plugin.debug(sender.getName() + " is trusting " + playerToTrust.getName() + " to an account");
        return true;
    }

    public static void trust(BankingPlugin plugin, Player executor, Account account, OfflinePlayer playerToTrust) {
        ClickType.removeClickType(executor);

        if (!account.isOwner(executor) && !executor.hasPermission(Permissions.ACCOUNT_TRUST_OTHER)) {
            if (account.isTrusted(executor)) {
                executor.sendMessage(Message.MUST_BE_OWNER.translate());
                return;
            }
            executor.sendMessage(Message.NO_PERMISSION_ACCOUNT_TRUST_OTHER.translate());
            return;
        }

        if (account.isTrusted(playerToTrust)) {
            plugin.debugf("%s was already trusted on that account (#%d)", playerToTrust.getName(), account.getID());
            Message message = account.isOwner(playerToTrust) ? Message.ALREADY_OWNER : Message.ALREADY_COOWNER;
            executor.sendMessage(message.with(Placeholder.PLAYER).as(playerToTrust.getName()).translate());
            return;
        }

        AccountTrustEvent event = new AccountTrustEvent(executor, account, playerToTrust);
        event.fire();
        if (event.isCancelled()) {
            plugin.debug("Account trust event cancelled");
            return;
        }

        plugin.debugf("%s has trusted %s to account #%d", executor.getName(), playerToTrust.getName(), account.getID());
        executor.sendMessage(Message.ADDED_COOWNER.with(Placeholder.PLAYER).as(playerToTrust.getName()).translate());
        account.trustPlayer(playerToTrust);
        plugin.getDatabase().addCoOwner(account, playerToTrust, null);
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

}
