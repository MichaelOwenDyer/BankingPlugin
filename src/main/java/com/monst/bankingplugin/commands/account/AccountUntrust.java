package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.events.account.AccountUntrustCommandEvent;
import com.monst.bankingplugin.events.account.AccountUntrustEvent;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.ClickType;
import com.monst.bankingplugin.utils.Permission;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class AccountUntrust extends SubCommand {

    AccountUntrust(BankingPlugin plugin) {
		super(plugin, "untrust", true);
    }

    @Override
    protected Permission getPermission() {
        return Permission.ACCOUNT_TRUST;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_UNTRUST;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        Player p = (Player) sender;
        plugin.debugf("%s wants to untrust a player from an account", p.getName());

        if (Permission.ACCOUNT_TRUST.notOwnedBy(p)) {
            p.sendMessage(Message.NO_PERMISSION_ACCOUNT_UNTRUST.translate());
            return true;
        }

        if (args.length < 1)
            return false;

        OfflinePlayer playerToUntrust = Utils.getPlayer(args[0]);
        if (playerToUntrust == null) {
            p.sendMessage(Message.PLAYER_NOT_FOUND.with(Placeholder.INPUT).as(args[0]).translate());
            return true;
        }

        AccountUntrustCommandEvent event = new AccountUntrustCommandEvent(p, args);
        event.fire();
        if (event.isCancelled()) {
            plugin.debug("Account untrust command event cancelled");
            return true;
        }

        p.sendMessage(Message.CLICK_ACCOUNT_UNTRUST.with(Placeholder.PLAYER).as(playerToUntrust.getName()).translate());
        ClickType.setUntrustClickType(p, playerToUntrust);
        plugin.debugf("%s is untrusting %s from an account", p.getName(), playerToUntrust.getName());
        return true;
    }

    public static void untrust(BankingPlugin plugin, Player executor, Account account, OfflinePlayer playerToUntrust) {
        ClickType.removeClickType(executor);

        if (!account.isOwner(executor) && Permission.ACCOUNT_TRUST_OTHER.notOwnedBy(executor)) {
            if (account.isTrusted(executor)) {
                executor.sendMessage(Message.MUST_BE_OWNER.translate());
                return;
            }
            executor.sendMessage(Message.NO_PERMISSION_ACCOUNT_UNTRUST_OTHER.translate());
            return;
        }

        if (!account.isCoOwner(playerToUntrust)) {
            plugin.debugf("%s was not a co-owner of account #%d and could not be removed",
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
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length != 1)
            return Collections.emptyList();
        return Utils.filter(Utils.getOnlinePlayerNames(), name -> Utils.startsWithIgnoreCase(name, args[0]));
    }

}
