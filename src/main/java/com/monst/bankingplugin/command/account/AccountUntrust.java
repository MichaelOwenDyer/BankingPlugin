package com.monst.bankingplugin.command.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.PlayerSubCommand;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.event.account.AccountUntrustCommandEvent;
import com.monst.bankingplugin.event.account.AccountUntrustEvent;
import com.monst.bankingplugin.exception.CancelledException;
import com.monst.bankingplugin.exception.ExecutionException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.command.ClickAction;
import com.monst.bankingplugin.util.Permission;
import com.monst.bankingplugin.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AccountUntrust extends PlayerSubCommand {

    AccountUntrust(BankingPlugin plugin) {
		super(plugin, "untrust");
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
    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION_ACCOUNT_UNTRUST;
    }

    @Override
    protected int getMinimumArguments() {
        return 1;
    }

    @Override
    protected void execute(Player player, String[] args) throws ExecutionException, CancelledException {
        OfflinePlayer playerToUntrust = Utils.getPlayer(args[0]);
        if (playerToUntrust == null)
            throw new ExecutionException(plugin, Message.PLAYER_NOT_FOUND.with(Placeholder.INPUT).as(args[0]));

        new AccountUntrustCommandEvent(player, args).fire();

        player.sendMessage(Message.CLICK_ACCOUNT_UNTRUST.with(Placeholder.PLAYER).as(playerToUntrust.getName()).translate(plugin));
        ClickAction.setAccountClickAction(player, account -> untrust(player, account, playerToUntrust));
        plugin.debugf("%s is untrusting %s from an account", player.getName(), playerToUntrust.getName());
    }

    private void untrust(Player executor, Account account, OfflinePlayer playerToUntrust) throws ExecutionException, CancelledException {
        ClickAction.remove(executor);

        if (!account.isOwner(executor) && Permission.ACCOUNT_TRUST_OTHER.notOwnedBy(executor)) {
            if (account.isTrusted(executor))
                throw new ExecutionException(plugin, Message.MUST_BE_OWNER);
            throw new ExecutionException(plugin, Message.NO_PERMISSION_ACCOUNT_UNTRUST_OTHER);
        }

        if (!account.isCoOwner(playerToUntrust))
            throw new ExecutionException(plugin, Message.NOT_A_CO_OWNER.with(Placeholder.PLAYER).as(playerToUntrust.getName()));

        new AccountUntrustEvent(executor, account, playerToUntrust).fire();

        plugin.debugf("%s has untrusted %s from account #%d", executor.getName(), playerToUntrust.getName(), account.getID());
        executor.sendMessage(Message.REMOVED_CO_OWNER.with(Placeholder.PLAYER).as(playerToUntrust.getName()).translate(plugin));
        account.untrustPlayer(playerToUntrust);
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length != 1)
            return Collections.emptyList();
        return Bukkit.getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> Utils.startsWithIgnoreCase(name, args[0]))
                .sorted()
                .collect(Collectors.toList());
    }

}
