package com.monst.bankingplugin.commands.control;

import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.config.values.ConfigValue;
import com.monst.bankingplugin.events.control.PluginConfigureCommandEvent;
import com.monst.bankingplugin.exceptions.parse.ArgumentParseException;
import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;
import com.monst.bankingplugin.utils.Permissions;
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
        ConfigValue<?> configValue = Config.getByPath(path);
        if (configValue == null) {
            sender.sendMessage(LangUtils.getMessage(Message.NOT_A_CONFIG_VALUE,
                    new Replacement(Placeholder.STRING, path)
            ));
            return true;
        }

        String input = Arrays.stream(args).skip(2).collect(Collectors.joining(" "));

        PluginConfigureCommandEvent event = new PluginConfigureCommandEvent(sender, configValue, input);
        event.fire();
        if (event.isCancelled() && !sender.hasPermission(Permissions.BYPASS_EXTERNAL_PLUGINS)) {
            PLUGIN.debug("Plugin configure event cancelled");
            return true;
        }

        String previousValue = configValue.getFormatted();

        boolean restartRequired;

        try {
            restartRequired = !configValue.set(input);
        } catch (ArgumentParseException e) {
            sender.sendMessage(e.getLocalizedMessage());
            PLUGIN.debugf("Could not parse argument: \"%s\"", e.getLocalizedMessage());
            return true;
        }

        String newValue = configValue.getFormatted();

        PLUGIN.debugf("%s has set %s from %s to %s", sender.getName(), configValue.getPath(), previousValue, newValue);
        sender.sendMessage(LangUtils.getMessage(Message.CONFIG_VALUE_SET,
                new Replacement(Placeholder.PROPERTY, path),
                new Replacement(Placeholder.PREVIOUS_VALUE, previousValue),
                new Replacement(Placeholder.VALUE, newValue)
        ));
        if (restartRequired) {
            sender.sendMessage(LangUtils.getMessage(Message.RESTART_REQUIRED,
                    new Replacement(Placeholder.PROPERTY, path)
            ));
        }
        return true;
    }

    @Override
    protected List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.CONFIG))
            return Collections.emptyList();

        if (args.length == 2)
            return Config.matchPath(args[1]);

        ConfigValue<?> configValue = Config.getByPath(args[1]);

        if (configValue == null)
            return Collections.emptyList();

        if (args.length == 3)
            return configValue.getTabCompletions();

        return Collections.emptyList();
    }

}
