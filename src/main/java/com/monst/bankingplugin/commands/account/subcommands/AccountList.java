package com.monst.bankingplugin.commands.account.subcommands;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.gui.AccountListGui;
import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AccountList extends AccountSubCommand {

    public AccountList() {
        super("list", false);
    }

    @Override
    public String getHelpMessage(CommandSender sender) {
        return Messages.COMMAND_USAGE_ACCOUNT_LIST;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " wants to list accounts");
        Collection<Account> accounts = null;
        String noAccountsMessage = "";
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                plugin.debug("Only players can list their own accounts");
                sender.sendMessage(Messages.PLAYER_COMMAND_ONLY);
                return true;
            }
            plugin.debug(sender.getName() + " has listed their own accounts");
            accounts = accountUtils.getAccountsCopy(a -> a.isOwner((Player) sender));
            noAccountsMessage = Messages.NO_ACCOUNTS_TO_LIST;
        } else if (args.length == 2) {
            if (args[1].equalsIgnoreCase("-a") || args[1].equalsIgnoreCase("all")) {
                if (!sender.hasPermission(Permissions.ACCOUNT_LIST_OTHER)) {
                    plugin.debug(sender.getName() + " does not have permission to view a list of other accounts");
                    sender.sendMessage(Messages.NO_PERMISSION_ACCOUNT_LIST_OTHER);
                    return true;
                }
                plugin.debug(sender.getName() + " has listed all accounts");
                accounts = accountUtils.getAccountsCopy();
                noAccountsMessage = Messages.NO_ACCOUNTS_TO_LIST;
            } else {
                OfflinePlayer owner = Utils.getPlayer(args[1]);
                if (owner == null) {
                    sender.sendMessage(String.format(Messages.PLAYER_NOT_FOUND, args[1]));
                    return true;
                }
                if (!sender.hasPermission(Permissions.ACCOUNT_LIST_OTHER) && (!(sender instanceof Player)
                        || !Utils.samePlayer((Player) sender, owner))) {
                    plugin.debug(sender.getName() + " does not have permission to view a list of other accounts");
                    sender.sendMessage(Messages.NO_PERMISSION_ACCOUNT_LIST_OTHER);
                    return true;
                }
                plugin.debug(sender.getName() + " has listed " + owner.getName() + "'s accounts");
                accounts = accountUtils.getAccountsCopy(a -> a.isOwner(owner));
                noAccountsMessage = Messages.NO_PLAYER_ACCOUNTS;
            }
        }
        if (accounts == null || accounts.isEmpty()) {
            sender.sendMessage(noAccountsMessage);
            return true;
        }

        if (sender instanceof Player)
            new AccountListGui(accounts).open(((Player) sender));
        else {
            int i = 0;
            for (Account account : accounts)
                sender.sendMessage(ChatColor.AQUA + "" + ++i + ". " + account.getColorizedName());
        }
        return true;
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        ArrayList<String> returnCompletions = new ArrayList<>();

        List<String> onlinePlayers = Utils.getOnlinePlayerNames(plugin);
        onlinePlayers.remove(sender.getName());

        if (args.length == 2) {
            if (sender.hasPermission(Permissions.ACCOUNT_LIST_OTHER) && ("-a".startsWith(args[1].toLowerCase()) || "all".startsWith(args[1].toLowerCase())))
                returnCompletions.add("all");
            returnCompletions.addAll(Utils.filter(onlinePlayers, name -> name.toLowerCase().startsWith(args[1].toLowerCase())));
            return returnCompletions;
        }
        return Collections.emptyList();
    }

}
