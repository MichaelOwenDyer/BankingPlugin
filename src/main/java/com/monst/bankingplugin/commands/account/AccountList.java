package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.gui.AccountListGui;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class AccountList extends AccountCommand.SubCommand {

    AccountList() {
        super("list", false);
    }

    @Override
    protected String getHelpMessage(CommandSender sender) {
        return Messages.COMMAND_USAGE_ACCOUNT_LIST;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " wants to list accounts");

        if (!sender.hasPermission(Permissions.ACCOUNT_LIST_OTHER)) {
            if (!(sender instanceof Player)) {
                plugin.debug("Only players can list their own accounts");
                sender.sendMessage(Messages.PLAYER_COMMAND_ONLY);
                return true;
            }
        }

        List<Account> accounts = lookupAccounts(sender, args);

        if (accounts.isEmpty()) {
            sender.sendMessage(String.format(Messages.NONE_FOUND, "accounts", "list"));
            return true;
        }

        if (sender instanceof Player) {
            new AccountListGui(() -> lookupAccounts(sender, args)).open(((Player) sender));
        } else {
            int i = 0;
            for (Account account : accounts)
                sender.sendMessage(ChatColor.AQUA + "" + ++i + ". " + account.getColorizedName());
        }
        return true;
    }

    private List<Account> lookupAccounts(CommandSender sender, String[] args) {
        List<Account> accounts;
        if (!sender.hasPermission(Permissions.ACCOUNT_LIST_OTHER)) {
            accounts = accountUtils.getAccountsCopy().stream()
                    .filter(a -> a.isOwner((Player) sender))
                    .sorted(Comparator.comparing(Account::getBalance).reversed())
                    .collect(Collectors.toList());
        } else if (args.length == 1) {
            accounts = accountUtils.getAccountsCopy().stream()
                    .sorted(Comparator.comparing(Account::getBalance).reversed())
                    .collect(Collectors.toList());
        } else {
            List<OfflinePlayer> players = Arrays.stream(args)
                    .map(Utils::getPlayer)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            accounts = accountUtils.getAccountsCopy().stream()
                    .filter(a -> players.contains(a.getOwner()))
                    .sorted(Comparator.<Account, Integer>comparing(a -> players.indexOf(a.getOwner()))
                            .thenComparing(Account::getBalance, BigDecimal::compareTo).reversed())
                    .collect(Collectors.toList());
        }
        return accounts;
    }

    @Override
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.ACCOUNT_LIST_OTHER))
            return Collections.emptyList();
        List<String> argList = Arrays.asList(args);
        return Utils.filter(Utils.getOnlinePlayerNames(plugin), name -> !argList.contains(name));
    }

}
