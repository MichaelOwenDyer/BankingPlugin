package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.commands.ConfirmableSubCommand;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountRemoveAllEvent;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class AccountRemoveAll extends SubCommand.AccountSubCommand implements ConfirmableSubCommand {

    AccountRemoveAll(BankingPlugin plugin) {
		super(plugin, "removeall", false);
    }

    @Override
    protected String getPermission() {
        return Permissions.ACCOUNT_REMOVEALL;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_REMOVE_ALL;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {

        plugin.debug(sender.getName() + " wants to remove all accounts");

        if (!sender.hasPermission(Permissions.ACCOUNT_REMOVEALL)) {
            plugin.debug(sender.getName() + " does not have permission to remove all accounts");
            sender.sendMessage(Message.NO_PERMISSION_ACCOUNT_REMOVEALL.translate());
            return true;
        }

        Set<Account> accounts;

        if (args.length == 1) {
            accounts = plugin.getAccountRepository().getAll();
        } else {
            Map<String, Player> namePlayerMap = plugin.getServer().getOnlinePlayers().stream().collect(
                    Collectors.toMap(
                            HumanEntity::getName,
                            e -> e
                    )
            );
            Set<OfflinePlayer> owners = new HashSet<>();
            for (String arg : args) {
                if (namePlayerMap.containsKey(arg))
                    owners.add(namePlayerMap.get(arg));
                else {
                    OfflinePlayer player = Utils.getPlayer(arg);
                    if (player != null)
                        owners.add(player);
                }
            }
            accounts = plugin.getAccountRepository().getMatching(a -> owners.contains(a.getOwner()));
        }

        if (accounts == null || accounts.isEmpty()) {
            sender.sendMessage(Message.ACCOUNTS_NOT_FOUND.translate());
            return true;
        }

        if (sender instanceof Player && Config.confirmOnRemoveAll.get() && !isConfirmed((Player) sender, args)) {
            sender.sendMessage(Message.ACCOUNT_CONFIRM_REMOVE_ALL
                    .with(Placeholder.NUMBER_OF_ACCOUNTS).as(accounts.size())
                    .translate());
            sender.sendMessage(Message.EXECUTE_AGAIN_TO_CONFIRM.translate());
            return true;
        }

        AccountRemoveAllEvent event = new AccountRemoveAllEvent(sender, accounts);
        event.fire();
        if (event.isCancelled()) {
            plugin.debug("Removeall event cancelled");
            return true;
        }
        plugin.debug(sender.getName() + " removed account(s) " + Utils.map(accounts, a -> "#" + a.getID()).toString());
        sender.sendMessage(Message.ALL_ACCOUNTS_REMOVED.with(Placeholder.NUMBER_OF_ACCOUNTS).as(accounts.size()).translate());
        accounts.forEach(a -> plugin.getAccountRepository().remove(a, true));
        return true;
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (!player.hasPermission(Permissions.ACCOUNT_REMOVEALL))
            return Collections.emptyList();
        List<String> argList = Arrays.asList(args);
        return Utils.filter(Utils.getOnlinePlayerNames(), name -> !argList.contains(name));
    }

}
