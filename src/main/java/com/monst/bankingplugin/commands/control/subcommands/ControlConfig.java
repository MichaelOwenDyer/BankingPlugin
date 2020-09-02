package com.monst.bankingplugin.commands.control.subcommands;

import com.monst.bankingplugin.utils.Messages;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ControlConfig extends ControlSubCommand {

    public ControlConfig() {
        super("config", false);
    }

    @Override
    public String getHelpMessage(CommandSender sender) {
        return sender.hasPermission(Permissions.CONFIG) ? Messages.COMMAND_USAGE_CONFIG : "";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " wants to configure the plugin");

        if (!sender.hasPermission(Permissions.CONFIG)) {
            plugin.debug(sender.getName() + " does not have permission to configure the config");
            sender.sendMessage(Messages.NO_PERMISSION_CONFIG);
            return true;
        }

        if (args.length < 4)
            return false;

        String property = args[2];
        StringBuilder sb = new StringBuilder(args[3]);
        for (int i = 4; i < args.length; i++) {
            sb.append(" ").append(args[i]);
        }
        String value = sb.toString();

        switch (args[1].toLowerCase()) {
            case "set":
                plugin.getPluginConfig().set(property, value);
                sender.sendMessage(String.format(Messages.CONFIG_VALUE_SET, property, value));
                break;
            case "add":
                plugin.getPluginConfig().add(property, value);
                sender.sendMessage(String.format(Messages.CONFIG_VALUE_ADDED, property));
                break;
            case "remove":
                plugin.getPluginConfig().remove(property, value);
                sender.sendMessage(String.format(Messages.CONFIG_VALUE_REMOVED, property));
                break;
            default:
                return false;
        }
        return true;
    }

    public List<String> getTabCompletions(CommandSender sender, String[] args) {
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

}
