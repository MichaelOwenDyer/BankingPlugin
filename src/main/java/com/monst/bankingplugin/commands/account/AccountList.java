package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.events.account.AccountListEvent;
import com.monst.bankingplugin.gui.AccountListGUI;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AccountList extends SubCommand {

    AccountList(BankingPlugin plugin) {
		super(plugin, "list", true);
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_LIST;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        plugin.debugf("%s wants to list accounts", sender.getName());

        Player player = (Player) sender;
        AccountListEvent event = new AccountListEvent(player, Collections.emptyList()); // FIXME: Pass accounts being listed
        event.fire();
        if (event.isCancelled()) {
            plugin.debug("Account list event cancelled");
            return true;
        }

        Supplier<Collection<Account>> getVisibleAccounts;
        if (!player.hasPermission(Permissions.ACCOUNT_LIST_OTHER))
            getVisibleAccounts = () -> plugin.getAccountRepository().getMatching(account -> account.isTrusted(player));
        else if (args.length == 1)
            getVisibleAccounts = plugin.getAccountRepository()::getAll;
        else {
            Set<OfflinePlayer> players = Arrays.stream(args)
                    .map(Utils::getPlayer)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            getVisibleAccounts = () -> plugin.getAccountRepository().getMatching(account -> players.contains(account.getOwner()));
        }
        new AccountListGUI(getVisibleAccounts).open(player);
        return true;
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (!player.hasPermission(Permissions.ACCOUNT_LIST_OTHER))
            return Collections.emptyList();
        List<String> argList = Arrays.asList(args);
        return Utils.filter(Utils.getOnlinePlayerNames(), name -> !argList.contains(name));
    }

}
