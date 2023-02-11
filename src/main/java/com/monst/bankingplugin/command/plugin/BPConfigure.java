package com.monst.bankingplugin.command.plugin;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.Permission;
import com.monst.bankingplugin.command.SubCommand;
import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.configuration.type.ConfigurationValue;
import com.monst.bankingplugin.event.control.PluginConfigureCommandEvent;
import com.monst.bankingplugin.exception.CommandExecutionException;
import com.monst.bankingplugin.exception.EventCancelledException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.command.Permissions;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BPConfigure extends SubCommand {
    
    BPConfigure(BankingPlugin plugin) {
		super(plugin, "configure");
    }

    @Override
    protected Permission getPermission() {
        return Permissions.CONFIGURE;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_CONFIGURE;
    }

    @Override
    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION_CONFIGURE;
    }

    @Override
    protected int getMinimumArguments() {
        return 1;
    }

    @Override
    protected void execute(CommandSender sender, String[] args) throws CommandExecutionException, EventCancelledException {
        String path = args[0];
        ConfigurationValue<?> configValue = plugin.config().findByPath(path);
        if (configValue == null)
            throw err(Message.NOT_A_CONFIGURATION_VALUE.with(Placeholder.INPUT).as(path));

        String input = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));

        try {
            new PluginConfigureCommandEvent(sender, configValue, input).fire();
        } catch (EventCancelledException e) {
            if (Permissions.BYPASS_EXTERNAL_PLUGINS.notOwnedBy(sender))
                throw e;
        }

        String previousValue = configValue.toString();

        if (input.isEmpty()) {
            configValue.reset();
        } else {
            try {
                configValue.parseAndSet(input);
            } catch (ArgumentParseException e) {
                throw err(e.getTranslatableMessage());
            }
        }

        String newValue = configValue.toString();

        plugin.debug("%s has configured %s from %s to %s", sender.getName(), configValue.getPath(), previousValue, newValue);
        sender.sendMessage(Message.CONFIGURATION_VALUE_SET
                .with(Placeholder.PROPERTY).as(path)
                .and(Placeholder.PREVIOUS_VALUE).as(previousValue)
                .and(Placeholder.VALUE).as(newValue)
                .translate(plugin));
        if (!configValue.isHotSwappable())
            sender.sendMessage(Message.RESTART_REQUIRED.with(Placeholder.PROPERTY).as(path).translate(plugin));
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1)
            return plugin.config().paths().stream()
                    .filter(path -> StringUtils.startsWithIgnoreCase(path, args[0])) // TODO: Use a containsIgnoreCase method instead
                    .sorted()
                    .collect(Collectors.toList());
        ConfigurationValue<?> configValue = plugin.config().findByPath(args[0]);
        if (configValue == null)
            return Collections.emptyList();
        return configValue.getTabCompletions(player, args);
    }

}
