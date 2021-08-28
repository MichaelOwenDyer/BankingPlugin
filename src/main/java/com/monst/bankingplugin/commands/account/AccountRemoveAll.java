package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.commands.PlayerCache;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountRemoveAllEvent;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.Permission;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class AccountRemoveAll extends SubCommand {

    AccountRemoveAll(BankingPlugin plugin) {
        super(plugin, "removeall", false);
    }

    @Override
    protected Permission getPermission() {
        return Permission.ACCOUNT_REMOVEALL;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_REMOVE_ALL;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {

        plugin.debugf("%s wants to remove all accounts", sender.getName());

        if (Permission.ACCOUNT_REMOVEALL.notOwnedBy(sender)) {
            plugin.debugf("%s does not have permission to remove all accounts", sender.getName());
            sender.sendMessage(Message.NO_PERMISSION_ACCOUNT_REMOVEALL.translate());
            return true;
        }

        Set<Account> accounts;

        if (args.length == 1) {
            accounts = plugin.getAccountRepository().getAll();
        } else {
            Map<String, UUID> namePlayerMap = plugin.getServer().getOnlinePlayers().stream().collect(
                    Collectors.toMap(
                            HumanEntity::getName,
                            Entity::getUniqueId
                    )
            );
            Set<UUID> owners = new HashSet<>();
            for (String arg : args) {
                if (namePlayerMap.containsKey(arg))
                    owners.add(namePlayerMap.get(arg));
                else {
                    OfflinePlayer player = Utils.getPlayer(arg);
                    if (player != null)
                        owners.add(player.getUniqueId());
                }
            }
            accounts = plugin.getAccountRepository().getMatching(account -> owners.contains(account.getOwner().getUniqueId()));
        }

        if (accounts == null || accounts.isEmpty()) {
            sender.sendMessage(Message.ACCOUNTS_NOT_FOUND.translate());
            return true;
        }

        if (sender instanceof Player && Config.confirmOnRemoveAll.get() && !PlayerCache.put((Player) sender, accounts)) {
            sender.sendMessage(Message.ACCOUNT_CONFIRM_REMOVE_ALL
                    .with(Placeholder.NUMBER_OF_ACCOUNTS).as(accounts.size())
                    .translate());
            sender.sendMessage(Message.EXECUTE_AGAIN_TO_CONFIRM.translate());
            return true;
        }

        AccountRemoveAllEvent event = new AccountRemoveAllEvent(sender, accounts);
        event.fire();
        if (event.isCancelled()) {
            plugin.debug("Remove all event cancelled");
            return true;
        }
        plugin.debugf("%s removed account(s) %s", sender.getName(), Utils.map(accounts, a -> "#" + a.getID()));
        sender.sendMessage(Message.ALL_ACCOUNTS_REMOVED.with(Placeholder.NUMBER_OF_ACCOUNTS).as(accounts.size()).translate());
        accounts.forEach(account -> plugin.getAccountRepository().remove(account, true));
        return true;
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (Permission.ACCOUNT_REMOVEALL.notOwnedBy(player))
            return Collections.emptyList();
        List<String> argList = Arrays.asList(args);
        return Utils.filter(Utils.getOnlinePlayerNames(), name -> !argList.contains(name));
    }

}
