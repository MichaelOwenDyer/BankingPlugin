package com.monst.bankingplugin.commands.account.subcommands;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.commands.ConfirmableSubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountRemoveAllEvent;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class AccountRemoveall extends AccountSubCommand implements ConfirmableSubCommand {

    public AccountRemoveall() {
        super("removeall", false);
    }

    @Override
    public String getHelpMessage(CommandSender sender) {
        return sender.hasPermission(Permissions.ACCOUNT_REMOVEALL) ? Messages.COMMAND_USAGE_ACCOUNT_REMOVEALL : "";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        plugin.debug(sender.getName() + " wants to remove all accounts");

        if (!sender.hasPermission(Permissions.ACCOUNT_REMOVEALL)) {
            plugin.debug(sender.getName() + " does not have permission to remove all accounts");
            sender.sendMessage(Messages.NO_PERMISSION_ACCOUNT_REMOVEALL);
            return true;
        }

        Set<Account> accounts;

        if (args.length == 1) {
            accounts = accountUtils.getAccountsCopy();
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
            accounts = accountUtils.getAccountsCopy(a -> owners.contains(a.getOwner()));
        }

        if (accounts == null || accounts.isEmpty()) {
            sender.sendMessage(String.format(Messages.NONE_FOUND, "accounts", "remove"));
            return true;
        }

        if (sender instanceof Player && Config.confirmOnRemoveAll && !isConfirmed((Player) sender, args)) {
            sender.sendMessage(String.format(Messages.ABOUT_TO_REMOVE_ACCOUNTS, accounts.size(), accounts.size() == 1 ? "" : "s"));
            sender.sendMessage(Messages.EXECUTE_AGAIN_TO_CONFIRM);
            return true;
        }

        AccountRemoveAllEvent event = new AccountRemoveAllEvent(sender, accounts);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Removeall event cancelled");
            return true;
        }
        plugin.debug(sender.getName() + " removed account(s) " + Utils.map(accounts, a -> "#" + a.getID()).toString());
        sender.sendMessage(String.format(Messages.ACCOUNTS_REMOVED,
                accounts.size(),
                accounts.size() == 1 ? " was" : "s were"));
        accountUtils.removeAccounts(accounts, true);
        return true;
    }

    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.ACCOUNT_REMOVEALL))
            return Collections.emptyList();
        List<String> argList = Arrays.asList(args);
        return Utils.filter(Utils.getOnlinePlayerNames(plugin), name -> !argList.contains(name));
    }

}
