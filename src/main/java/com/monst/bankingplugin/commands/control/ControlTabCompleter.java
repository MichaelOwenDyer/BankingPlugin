package com.monst.bankingplugin.commands.control;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.utils.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ControlTabCompleter implements TabCompleter {

    private final BankingPlugin plugin;

    public ControlTabCompleter(BankingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        final String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "config":
                return completeControlConfig(sender, args);
            case "reload": case "update":
                break;
        }
        return Collections.emptyList();
    }

    private List<String> completeControlConfig(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.CONFIG))
            return Collections.emptyList();

        ArrayList<String> returnCompletions = new ArrayList<>();

        if (args.length == 2)
            if (!args[1].isEmpty()) {
                if ("set".startsWith(args[1].toLowerCase()))
                    return Collections.singletonList("set");
            } else
                return Collections.singletonList("set");

        Set<String> configValues = plugin.getConfig().getKeys(true);
        plugin.getConfig().getKeys(true).forEach(s -> {
            if (s.contains("."))
                configValues.remove(s.substring(0, s.lastIndexOf('.')));
        });

        if (args.length == 3) {
            if (!args[2].isEmpty()) {
                for (String s : configValues)
                    if (s.contains(args[2]))
                        returnCompletions.add(s);
                return returnCompletions;
            } else
                return new ArrayList<>(configValues);
        } else if (args.length == 4) {
            List<?> values = plugin.getConfig().getList(args[2]);
            if (values != null) {
                if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove"))
                    for (Object o : values)
                        returnCompletions.add("-" + o.toString());
                else
                    returnCompletions.add(values.stream().map(o -> "-" + o.toString()).collect(Collectors.joining(" ")));
                return returnCompletions;
            }
            Object value = plugin.getConfig().get(args[2]);
            if (value != null) {
                returnCompletions.add(value.toString());
            }
            return returnCompletions;
        }
        return Collections.emptyList();
    }
}
