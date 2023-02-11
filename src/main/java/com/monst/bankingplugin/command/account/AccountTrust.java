package com.monst.bankingplugin.command.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.ClickAction;
import com.monst.bankingplugin.command.Permission;
import com.monst.bankingplugin.command.PlayerSubCommand;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.event.account.AccountTrustCommandEvent;
import com.monst.bankingplugin.event.account.AccountTrustEvent;
import com.monst.bankingplugin.exception.CommandExecutionException;
import com.monst.bankingplugin.exception.EventCancelledException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.command.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AccountTrust extends PlayerSubCommand {

    AccountTrust(BankingPlugin plugin) {
		super(plugin, "trust");
    }

    @Override
    protected Permission getPermission() {
        return Permissions.ACCOUNT_TRUST;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_TRUST;
    }

    @Override
    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION_ACCOUNT_TRUST;
    }

    @Override
    protected int getMinimumArguments() {
        return 1;
    }

    @Override
    protected void execute(Player player, String[] args) throws CommandExecutionException, EventCancelledException {
        OfflinePlayer playerToTrust = getPlayer(args[0]);
        if (playerToTrust == null)
            throw err(Message.PLAYER_NOT_FOUND.with(Placeholder.INPUT).as(args[0]));

        new AccountTrustCommandEvent(player, args).fire();

        player.sendMessage(Message.CLICK_ACCOUNT_TRUST.with(Placeholder.PLAYER).as(playerToTrust.getName()).translate(plugin));
        ClickAction.setAccountClickAction(player, account -> trust(player, account, playerToTrust));
        plugin.debug("%s is trusting %s to an account", player.getName(), playerToTrust.getName());
    }

    private void trust(Player executor, Account account, OfflinePlayer playerToTrust) throws CommandExecutionException, EventCancelledException {
        ClickAction.remove(executor);

        if (!account.isOwner(executor) && Permissions.ACCOUNT_TRUST_OTHER.notOwnedBy(executor)) {
            if (account.isTrusted(executor))
                throw err(Message.MUST_BE_OWNER);
            throw err(Message.NO_PERMISSION_ACCOUNT_TRUST_OTHER);
        }
    
        if (account.isOwner(playerToTrust))
            throw err(Message.ALREADY_OWNER.with(Placeholder.PLAYER).as(playerToTrust.getName()));
        if (account.isCoOwner(playerToTrust))
            throw err(Message.ALREADY_CO_OWNER.with(Placeholder.PLAYER).as(playerToTrust.getName()));

        new AccountTrustEvent(executor, account, playerToTrust).fire();
    
        account.trustPlayer(playerToTrust);
        executor.sendMessage(Message.ADDED_CO_OWNER.with(Placeholder.PLAYER).as(playerToTrust.getName()).translate(plugin));
        plugin.debug("%s has trusted %s to account %s", executor.getName(), playerToTrust.getName(), account);
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length != 1)
            return Collections.emptyList();
        return Bukkit.getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> StringUtil.startsWithIgnoreCase(name, args[0]))
                .sorted()
                .collect(Collectors.toList());
    }

}
