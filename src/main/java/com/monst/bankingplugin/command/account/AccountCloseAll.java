package com.monst.bankingplugin.command.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.Permission;
import com.monst.bankingplugin.command.SubCommand;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.event.account.AccountCloseAllEvent;
import com.monst.bankingplugin.exception.CommandExecutionException;
import com.monst.bankingplugin.exception.EventCancelledException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.command.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

public class AccountCloseAll extends SubCommand {

    AccountCloseAll(BankingPlugin plugin) {
        super(plugin, "closeall");
    }

    @Override
    protected Permission getPermission() {
        return Permissions.ACCOUNT_CLOSE_ALL;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_CLOSE_ALL;
    }

    @Override
    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION_ACCOUNT_CLOSE_ALL;
    }

    @Override
    protected void execute(CommandSender sender, String[] args) throws CommandExecutionException, EventCancelledException {
        Set<Account> accounts;
        if (args.length == 0)
            accounts = plugin.getAccountService().findAll();
        else {
            Set<OfflinePlayer> owners = Arrays.stream(args)
                    .map(SubCommand::getPlayer)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            accounts = plugin.getAccountService().findByOwners(owners);
        }

        if (accounts.isEmpty())
            throw err(Message.ACCOUNTS_NOT_FOUND);

        if (sender instanceof Player && plugin.config().confirmOnRemoveAll.get()
                && isFirstUsage((Player) sender, Objects.hash("closeAll", accounts))) {
            sender.sendMessage(Message.ABOUT_TO_CLOSE_ALL_ACCOUNTS
                    .with(Placeholder.NUMBER_OF_ACCOUNTS).as(accounts.size())
                    .translate(plugin));
            sender.sendMessage(Message.EXECUTE_AGAIN_TO_CONFIRM.translate(plugin));
            return;
        }

        new AccountCloseAllEvent(sender, accounts).fire();

        for (Account account : accounts) {
            account.getBank().removeAccount(account);
            account.resetChestTitle();
        }
        plugin.getAccountService().removeAll(accounts);
        plugin.debugf("%s removed account(s) %s", sender.getName(), accounts);
        sender.sendMessage(Message.ALL_ACCOUNTS_CLOSED.with(Placeholder.NUMBER_OF_ACCOUNTS).as(accounts.size()).translate(plugin));
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        List<String> argList = Arrays.asList(args);
        return Bukkit.getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> StringUtil.startsWithIgnoreCase(name, args[0]))
                .filter(name -> !argList.contains(name))
                .sorted()
                .collect(Collectors.toList());
    }

}
