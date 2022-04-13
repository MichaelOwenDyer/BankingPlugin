package com.monst.bankingplugin.command.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.PlayerSubCommand;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.event.account.AccountTransferCommandEvent;
import com.monst.bankingplugin.event.account.AccountTransferEvent;
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

public class AccountTransfer extends PlayerSubCommand {

    AccountTransfer(BankingPlugin plugin) {
		super(plugin, "transfer");
    }

    @Override
    protected Permission getPermission() {
        return Permission.ACCOUNT_TRANSFER;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_TRANSFER;
    }

    @Override
    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION_ACCOUNT_TRANSFER;
    }

    @Override
    protected int getMinimumArguments() {
        return 1;
    }

    @Override
    protected void execute(Player player, String[] args) throws ExecutionException, CancelledException {
        OfflinePlayer newOwner = Utils.getPlayer(args[0]);
        if (newOwner == null)
            throw new ExecutionException(plugin, Message.PLAYER_NOT_FOUND.with(Placeholder.INPUT).as(args[0]));

        new AccountTransferCommandEvent(player, args).fire();

        player.sendMessage(Message.CLICK_ACCOUNT_TRANSFER.with(Placeholder.PLAYER).as(newOwner.getName()).translate(plugin));
        ClickAction.setAccountClickAction(player, account -> transfer(player, account, newOwner));
        plugin.debugf("%s is transferring ownership of an account to %s", player.getName(), newOwner.getName());
    }

    private void transfer(Player player, Account account, OfflinePlayer newOwner) throws ExecutionException, CancelledException {
        plugin.debugf("%s is transferring ownership of account #%d to %s", player.getName(), account.getID(), newOwner.getName());

        if (!account.isOwner(player) && Permission.ACCOUNT_TRANSFER_OTHER.notOwnedBy(player)) {
            ClickAction.remove(player);
            if (account.isTrusted(player))
                throw new ExecutionException(plugin, Message.MUST_BE_OWNER);
            throw new ExecutionException(plugin, Message.NO_PERMISSION_ACCOUNT_TRANSFER_OTHER);
        }

        if (account.isOwner(newOwner)) {
            ClickAction.remove(player);
            throw new ExecutionException(plugin, Message.ALREADY_OWNER.with(Placeholder.PLAYER).as(newOwner.getName()));
        }

        if (plugin.config().confirmOnTransfer.get() && ClickAction.mustConfirm(player)) {
            plugin.debug("Account transfer needs confirmation");
            player.sendMessage(Message.ACCOUNT_ABOUT_TO_TRANSFER
                    .with(Placeholder.PLAYER).as(newOwner.getName())
                    .and(Placeholder.ACCOUNT_ID).as(account.getID())
                    .translate(plugin));
            player.sendMessage(Message.CLICK_AGAIN_TO_CONFIRM.translate(plugin));
            return;
        }
        ClickAction.remove(player);

        new AccountTransferEvent(player, account, newOwner).fire();

        if (plugin.config().trustOnTransfer.get())
            account.trustPlayer(account.getOwner());
        account.setOwner(newOwner);
        account.updateChestTitle();
        plugin.getAccountService().update(account);
        player.sendMessage(Message.ACCOUNT_TRANSFERRED.with(Placeholder.PLAYER).as(newOwner.getName()).translate(plugin));
        if (!player.getUniqueId().equals(newOwner.getUniqueId()))
            Utils.message(newOwner, Message.ACCOUNT_TRANSFERRED_TO_YOU.with(Placeholder.PLAYER).as(player.getName()).translate(plugin));
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
