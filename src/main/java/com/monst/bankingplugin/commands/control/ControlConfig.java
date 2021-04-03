package com.monst.bankingplugin.commands.control;

import com.monst.bankingplugin.config.values.ConfigField;
import com.monst.bankingplugin.exceptions.ArgumentParseException;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.Permissions;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ControlConfig extends ControlCommand.SubCommand {

    ControlConfig() {
        super("config", false);
    }

    @Override
    protected String getPermission() {
        return Permissions.CONFIG;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_CONFIG;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        plugin.debug(sender.getName() + " wants to configure the plugin");

        if (!sender.hasPermission(Permissions.CONFIG)) {
            plugin.debug(sender.getName() + " does not have permission to configure the config");
            sender.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_CONFIG));
            return true;
        }

        if (args.length < 3)
            return false;

        Message action;
        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "set":
                action = Message.CONFIG_VALUE_SET;
                break;
            case "add":
                action = Message.CONFIG_VALUE_ADDED;
                break;
            case "remove":
                action = Message.CONFIG_VALUE_REMOVED;
                break;
            default:
                return false;
        }

        String path = args[2];
        ConfigField field = ConfigField.getByName(path);
        if (field == null) {
            sender.sendMessage(LangUtils.getMessage(Message.NOT_A_CONFIG_VALUE,
                    new Replacement(Placeholder.STRING, path)
            ));
            return true;
        }

        StringBuilder sb = new StringBuilder(32);
        if (args.length > 3) {
            sb.append(args[3]);
            for (int i = 4; i < args.length; i++)
                sb.append(" ").append(args[i]);
        }
        String value = sb.toString();

        String previousValue = field.getConfigValue().getFormatted();

        switch (action) {
            case CONFIG_VALUE_SET:
                try {
                    plugin.getPluginConfig().set(field, path, value);
                } catch (ArgumentParseException e) {
                    sender.sendMessage(e.getLocalizedMessage());
                    return true;
                }
                break;
            case CONFIG_VALUE_ADDED:
                // plugin.getPluginConfig().add("", value);
                break;
            case CONFIG_VALUE_REMOVED:
                // plugin.getPluginConfig().remove("", value);
                break;
            default:
                return false;
        }

        String newValue = field.getConfigValue().getFormatted();

        sender.sendMessage(LangUtils.getMessage(action,
                new Replacement(Placeholder.PROPERTY, path),
                new Replacement(Placeholder.PREVIOUS_VALUE, previousValue),
                new Replacement(Placeholder.VALUE, newValue)
        ));
        return true;
    }

    @Override
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.CONFIG))
            return Collections.emptyList();

        if (args.length == 2)
            return Stream.of("set", "add", "remove")
                    .filter(option -> option.startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());

        ConfigField field = ConfigField.getByName(args[2]);

        if (args.length == 3) {
            if (field != null && field.isOverridable())
                return field.getOverridableValue().getPaths().stream()
                        .filter(path -> path.contains(args[2].toLowerCase(Locale.ROOT)))
                        .collect(Collectors.toList());
            return Arrays.stream(ConfigField.values())
                    .map(ConfigField::toString)
                    .filter(path -> path.contains(args[2].toLowerCase(Locale.ROOT)))
                    .sorted()
                    .collect(Collectors.toList());
        }

        if (field == null)
            return Collections.emptyList();

        if (args.length == 4) {
            if (field.isOverridable())
                return Arrays.asList("default", "allow-override");
            return Collections.singletonList(field.getConfigValue().getFormatted());
        }

        return Collections.emptyList();
    }

}
