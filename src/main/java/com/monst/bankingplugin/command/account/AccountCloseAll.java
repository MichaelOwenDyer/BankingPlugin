package com.monst.bankingplugin.command.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.CommandCache;
import com.monst.bankingplugin.command.SubCommand;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.event.account.AccountCloseAllEvent;
import com.monst.bankingplugin.exception.CancelledException;
import com.monst.bankingplugin.exception.ExecutionException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.util.Permission;
import com.monst.bankingplugin.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class AccountCloseAll extends SubCommand {

    AccountCloseAll(BankingPlugin plugin) {
        super(plugin, "closeall");
    }

    @Override
    protected Permission getPermission() {
        return Permission.ACCOUNT_CLOSE_ALL;
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
    protected void execute(CommandSender sender, String[] args) throws ExecutionException, CancelledException {
        List<Account> accounts;
        if (args.length == 0)
            accounts = plugin.getAccountService().findAll();
        else {
            Map<String, Player> namePlayerMap = plugin.getServer().getOnlinePlayers().stream().collect(
                    Collectors.toMap(
                            Player::getName,
                            player -> player
                    )
            );
            Set<OfflinePlayer> owners = new HashSet<>();
            for (String playerName : args) {
                if (namePlayerMap.containsKey(playerName))
                    owners.add(namePlayerMap.get(playerName));
                else {
                    OfflinePlayer player = Utils.getPlayer(playerName);
                    if (player != null)
                        owners.add(player);
                }
            }
            accounts = plugin.getAccountService().findByOwnerIn(owners);
        }

        if (accounts.isEmpty())
            throw new ExecutionException(plugin, Message.ACCOUNTS_NOT_FOUND);

        if (sender instanceof Player && plugin.config().confirmOnRemoveAll.get() &&
                CommandCache.isFirstUsage((Player) sender, Objects.hash("closeAll", new HashSet<>(accounts)))) {
            sender.sendMessage(Message.ABOUT_TO_CLOSE_ALL_ACCOUNTS
                    .with(Placeholder.NUMBER_OF_ACCOUNTS).as(accounts.size())
                    .translate(plugin));
            sender.sendMessage(Message.EXECUTE_AGAIN_TO_CONFIRM.translate(plugin));
            return;
        }

        new AccountCloseAllEvent(sender, accounts).fire();

        Set<Bank> banks = new HashSet<>();
        for (Account account : accounts) {
            Bank bank = account.getBank();
            bank.removeAccount(account);
            banks.add(bank);
            account.resetChestTitle();
        }
        plugin.getAccountService().removeAll(accounts);
        plugin.getBankService().updateAll(banks);
        plugin.debugf("%s removed account(s) %s", sender.getName(), accounts.stream().map(Account::getID).collect(Collectors.toList()));
        sender.sendMessage(Message.ALL_ACCOUNTS_CLOSED.with(Placeholder.NUMBER_OF_ACCOUNTS).as(accounts.size()).translate(plugin));
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        List<String> argList = Arrays.asList(args);
        return Bukkit.getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> Utils.startsWithIgnoreCase(name, args[0]))
                .filter(name -> !argList.contains(name))
                .sorted()
                .collect(Collectors.toList());
    }

}
