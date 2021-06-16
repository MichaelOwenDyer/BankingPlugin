package com.monst.bankingplugin.commands.control;

import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.config.values.ConfigField;
import com.monst.bankingplugin.exceptions.ArgumentParseException;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ControlConfigure extends ControlCommand.SubCommand {

    ControlConfigure() {
        super("configure", false);
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
        PLUGIN.debugf("%s wants to configure the plugin: '%s'", sender.getName(), String.join(" ", args));

        if (!sender.hasPermission(Permissions.CONFIG)) {
            PLUGIN.debug(sender.getName() + " does not have permission to configure the config");
            sender.sendMessage(LangUtils.getMessage(Message.NO_PERMISSION_CONFIG));
            return true;
        }

        if (args.length < 2)
            return false;

        String path = args[1];
        ConfigField field = ConfigField.getByName(path);
        if (field == null) {
            sender.sendMessage(LangUtils.getMessage(Message.NOT_A_CONFIG_VALUE,
                    new Replacement(Placeholder.STRING, path)
            ));
            return true;
        }

        String input = Arrays.stream(args).skip(2).collect(Collectors.joining(" "));

        String previousValue = field.getConfigValue().getFormatted();

        try {
            Config.set(field, input);
        } catch (ArgumentParseException e) {
            sender.sendMessage(e.getLocalizedMessage());
            PLUGIN.debugf("Could not parse argument: %s", e.getLocalizedMessage());
            return true;
        }

        String newValue = field.getConfigValue().getFormatted();

        PLUGIN.debugf("%s has set %s from %s to %s", sender.getName(), field.toString(), previousValue, newValue);
        sender.sendMessage(LangUtils.getMessage(Message.CONFIG_VALUE_SET,
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

        ConfigField field = ConfigField.getByName(args[1]);

        if (args.length == 2)
            return ConfigField.stream()
                    .map(ConfigField::toString)
                    .filter(path -> Utils.containsIgnoreCase(path, args[1]))
                    .sorted()
                    .collect(Collectors.toList());

        if (field == null)
            return Collections.emptyList();

        if (args.length == 3)
            return field.getConfigValue().getTabCompletions();

        return Collections.emptyList();
    }

}
