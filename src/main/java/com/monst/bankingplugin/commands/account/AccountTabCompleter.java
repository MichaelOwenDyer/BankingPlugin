package com.monst.bankingplugin.commands.account;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AccountTabCompleter implements TabCompleter {

    private final BankingPlugin plugin;

    public AccountTabCompleter(BankingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        final String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "create": case "remove": case "info": case "limits": case "migrate":
                break;
            case "list":
                return completeAccountList(sender, args);
            case "removeall":
                return completeAccountRemoveAll(sender, args);
            case "set":
                return completeAccountSet(args);
            case "trust": case "untrust":
                return completeAccountTrust((Player) sender, args);
            case "transfer":
                return completeAccountTransfer((Player) sender, args);
        }
        return Collections.emptyList();
    }

    private List<String> completeAccountList(CommandSender sender, String[] args) {
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

    private List<String> completeAccountRemoveAll(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.ACCOUNT_REMOVEALL))
            return Collections.emptyList();
        List<String> argList = Arrays.asList(args);
        return Utils.getOnlinePlayerNames(plugin).stream()
                .filter(name -> !argList.contains(name))
                .sorted()
                .collect(Collectors.toList());
    }

    private List<String> completeAccountSet(String[] args) {
        List<String> fields = Arrays.asList("nickname", "multiplier", "interest-delay");
        if (args.length == 2)
            return Utils.filter(fields, field -> field.startsWith(args[1].toLowerCase()));
        return Collections.emptyList();
    }

    private List<String> completeAccountTrust(Player p, String[] args) {
        List<String> onlinePlayers = Utils.getOnlinePlayerNames(plugin);
        onlinePlayers.remove(p.getName());
        if (args.length == 2)
            return Utils.filter(onlinePlayers, name -> name.toLowerCase().startsWith(args[1].toLowerCase()));
        return Collections.emptyList();
    }

    private List<String> completeAccountTransfer(Player p, String[] args) {
        List<String> returnCompletions = Utils.getOnlinePlayerNames(plugin);
        if (!p.hasPermission(Permissions.ACCOUNT_TRANSFER_OTHER))
            returnCompletions.remove(p.getName());
        if (p.hasPermission(Permissions.BANK_CREATE_ADMIN))
            returnCompletions.add("ADMIN");

        if (args.length == 2)
            return Utils.filter(returnCompletions, string -> string.toLowerCase().startsWith(args[1].toLowerCase()));
        return Collections.emptyList();
    }
}
