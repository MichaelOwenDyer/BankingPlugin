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

public class AccountList extends SubCommand.AccountSubCommand {

    AccountList(BankingPlugin plugin) {
		super(plugin, "list", false);
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_LIST;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " wants to list accounts");

        if (!sender.hasPermission(Permissions.ACCOUNT_LIST_OTHER) && !(sender instanceof Player)) {
            plugin.debug("Only players can list their own accounts");
            sender.sendMessage(Message.PLAYER_COMMAND_ONLY.translate());
            return true;
        }

        AccountListEvent event = new AccountListEvent(sender, Collections.emptyList()); // FIXME: Pass accounts being listed
        event.fire();
        if (event.isCancelled()) {
            plugin.debug("Account list event cancelled");
            return true;
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            Supplier<Collection<Account>> getVisibleAccounts = () -> {
                if (!player.hasPermission(Permissions.ACCOUNT_LIST_OTHER))
                    return plugin.getAccountRepository().getMatching(account -> account.isTrusted(player));
                else if (args.length == 1)
                    return plugin.getAccountRepository().getAll();
                Set<OfflinePlayer> players = Arrays.stream(args)
                        .map(Utils::getPlayer)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                return plugin.getAccountRepository().getMatching(account -> players.contains(account.getOwner()));
            };
            new AccountListGUI(getVisibleAccounts).open(player);
        } else {
            int i = 0;
            for (Account account : plugin.getAccountRepository().getAll())
                sender.sendMessage(Utils.colorize("&b" + ++i + ". &7" + account.getRawName() + "&7(#" + account.getID() + ")"));
        }
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
