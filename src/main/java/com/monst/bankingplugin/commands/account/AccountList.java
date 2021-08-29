package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.events.account.AccountListEvent;
import com.monst.bankingplugin.gui.AccountListGUI;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.utils.Permission;
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
        Player p = (Player) sender;
        plugin.debugf("%s wants to list accounts", sender.getName());

        AccountListEvent event = new AccountListEvent(p, Collections.emptyList()); // FIXME: Pass accounts being listed
        event.fire();
        if (event.isCancelled()) {
            plugin.debug("Account list event cancelled");
            return true;
        }

        Supplier<Collection<Account>> getVisibleAccounts;
        if (Permission.ACCOUNT_LIST_OTHER.notOwnedBy(p))
            getVisibleAccounts = () -> plugin.getAccountRepository().getMatching(account -> account.isTrusted(p));
        else if (args.length == 0)
            getVisibleAccounts = plugin.getAccountRepository()::getAll;
        else {
            Set<OfflinePlayer> players = Arrays.stream(args)
                    .map(Utils::getPlayer)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            getVisibleAccounts = () -> plugin.getAccountRepository().getMatching(account -> players.contains(account.getOwner()));
        }
        new AccountListGUI(getVisibleAccounts).open(p);
        return true;
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (Permission.ACCOUNT_LIST_OTHER.notOwnedBy(player))
            return Collections.emptyList();
        List<String> argList = Arrays.asList(args);
        return Utils.filter(Utils.getOnlinePlayerNames(), name -> !argList.contains(name));
    }

}
