package com.monst.bankingplugin.commands.control;

import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
                return completeConfig(sender, args);
            case "payinterest":
                return completePayInterest(sender, args);
            case "reload": case "update":
                break;
        }
        return Collections.emptyList();
    }

    private List<String> completeConfig(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.CONFIG))
            return Collections.emptyList();

        if (args.length == 2)
            return "set".startsWith(args[1].toLowerCase())
                    ? Collections.singletonList("set")
                    : Collections.emptyList();

        List<String> configValues = new ArrayList<>(plugin.getConfig().getKeys(true));
        plugin.getConfig().getKeys(true).forEach(s -> {
            if (s.contains("."))
                configValues.remove(s.substring(0, s.lastIndexOf('.')));
        });

        if (args.length == 3)
            return Utils.filter(configValues, value -> value.contains(args[2].toLowerCase()));
        else if (args.length == 4) {
            List<?> values = plugin.getConfig().getList(args[2]);
            if (values != null) {
                if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove"))
                    return Utils.map(values, String::valueOf);
                else
                    return Collections.singletonList(values.toString());
            }
            Object value = plugin.getConfig().get(args[2]);
            if (value != null) {
                return Collections.singletonList(value.toString());
            }
        }
        return Collections.emptyList();
    }

    private List<String> completePayInterest(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.PAY_INTEREST))
            return Collections.emptyList();
        return plugin.getBankUtils().getBanksCopy().stream()
                .map(Bank::getName)
                .filter(name -> !Arrays.asList(args).contains(name))
                .sorted()
                .collect(Collectors.toList());
    }
}
