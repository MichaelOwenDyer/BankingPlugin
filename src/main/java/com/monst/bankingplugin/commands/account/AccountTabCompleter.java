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
                return completeAccountSet((Player) sender, args);
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
            if (!args[1].isEmpty()) {
                if (sender.hasPermission(Permissions.ACCOUNT_LIST_OTHER) && ("-a".startsWith(args[1].toLowerCase()) || "all".startsWith(args[1].toLowerCase())))
                    returnCompletions.add("all");
                returnCompletions.addAll(onlinePlayers.stream().filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList()));
                return returnCompletions;
            } else {
                onlinePlayers.add("all");
                return onlinePlayers;
            }
        }
        return Collections.emptyList();
    }

    private List<String> completeAccountRemoveAll(CommandSender sender, String[] args) {
        ArrayList<String> returnCompletions = new ArrayList<>();
        List<String> onlinePlayers = Utils.getOnlinePlayerNames(plugin);
        onlinePlayers.remove(sender.getName());

        if (args.length == 2) {
            if (!args[1].isEmpty()) {
                if ("-a".startsWith(args[1].toLowerCase()) || "all".startsWith(args[1].toLowerCase()))
                    returnCompletions.add("all");
                if ("-c".startsWith(args[1].toLowerCase()) || "cancel".startsWith(args[1].toLowerCase()))
                    returnCompletions.add("cancel");
                for (String name : onlinePlayers)
                    if (name.toLowerCase().startsWith(args[1].toLowerCase()))
                        returnCompletions.add(name);
                return returnCompletions;
            } else {
                onlinePlayers.addAll(Arrays.asList("all", "cancel"));
                return onlinePlayers;
            }
        }
        return Collections.emptyList();
    }

    private List<String> completeAccountSet(Player p, String[] args) {
        ArrayList<String> returnCompletions = new ArrayList<>();
        List<String> fields = Arrays.asList("nickname", "multiplier", "interest-delay");

        if (args.length == 2) {
            if (!args[1].isEmpty()) {
                for (String s : fields)
                    if (s.startsWith(args[1]))
                        returnCompletions.add(s);
                return returnCompletions;
            } else
                return fields;
        }
        return Collections.emptyList();
    }

    private List<String> completeAccountTrust(Player p, String[] args) {
        ArrayList<String> returnCompletions = new ArrayList<>();
        List<String> onlinePlayers = Utils.getOnlinePlayerNames(plugin);
        onlinePlayers.remove(p.getName());

        if (args.length == 2) {
            if (!args[1].isEmpty()) {
                for (String name : onlinePlayers)
                    if (name.startsWith(args[1]))
                        returnCompletions.add(name);
                return returnCompletions;
            }
            return onlinePlayers;
        }
        return Collections.emptyList();
    }

    private List<String> completeAccountTransfer(Player p, String[] args) {
        ArrayList<String> returnCompletions = new ArrayList<>();
        List<String> onlinePlayers = Utils.getOnlinePlayerNames(plugin);
        if (!p.hasPermission(Permissions.ACCOUNT_TRANSFER_OTHER))
            onlinePlayers.remove(p.getName());
        if (p.hasPermission(Permissions.BANK_CREATE_ADMIN))
            onlinePlayers.add("ADMIN");

        if (args.length == 2) {
            if (!args[1].isEmpty()) {
                for (String name : onlinePlayers)
                    if (name.startsWith(args[1]))
                        returnCompletions.add(name);
                return returnCompletions;
            }
            return onlinePlayers;
        }
        return Collections.emptyList();
    }
}
