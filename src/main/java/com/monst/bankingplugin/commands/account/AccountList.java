package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.events.account.AccountListEvent;
import com.monst.bankingplugin.gui.AccountListGUI;
import com.monst.bankingplugin.lang.Messages;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class AccountList extends SubCommand.AccountSubCommand {

    AccountList() {
        super("list", false);
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_ACCOUNT_LIST;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        PLUGIN.debug(sender.getName() + " wants to list accounts");

        if (!sender.hasPermission(Permissions.ACCOUNT_LIST_OTHER)) {
            if (!(sender instanceof Player)) {
                PLUGIN.debug("Only players can list their own accounts");
                sender.sendMessage(Messages.get(Message.PLAYER_COMMAND_ONLY));
                return true;
            }
        }

        AccountListEvent event = new AccountListEvent(sender, Collections.emptyList()); // FIXME: Pass accounts being listed
        event.fire();
        if (event.isCancelled()) {
            PLUGIN.debug("Account list event cancelled");
            return true;
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            new AccountListGUI(() -> getVisibleAccounts(player, args)).open(player);
        } else {
            int i = 0;
            for (Account account : accountRepo.getAll())
                sender.sendMessage(Utils.colorize("&b" + ++i + ". &7" + account.getRawName() + "&7(#" + account.getID() + ")"));
        }
        return true;
    }

    private static Set<Account> getVisibleAccounts(Player player, String[] args) {
        if (!player.hasPermission(Permissions.ACCOUNT_LIST_OTHER))
            return accountRepo.getMatching(account -> account.isTrusted(player));
        else if (args.length == 1)
            return accountRepo.getAll();
        Set<OfflinePlayer> players = Arrays.stream(args)
                .map(Utils::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return accountRepo.getMatching(account -> players.contains(account.getOwner()));
    }

    @Override
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.ACCOUNT_LIST_OTHER))
            return Collections.emptyList();
        List<String> argList = Arrays.asList(args);
        return Utils.filter(Utils.getOnlinePlayerNames(), name -> !argList.contains(name));
    }

}
