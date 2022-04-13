package com.monst.bankingplugin.command.plugin;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.SubCommand;
import com.monst.bankingplugin.event.control.PluginConfigureCommandEvent;
import com.monst.bankingplugin.exception.CancelledException;
import com.monst.bankingplugin.exception.ExecutionException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.util.Permission;
import com.monst.bankingplugin.util.Utils;
import com.monst.pluginconfiguration.ConfigurationValue;
import com.monst.pluginconfiguration.exception.ArgumentParseException;
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
        return Permission.CONFIGURE;
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
    protected void execute(CommandSender sender, String[] args) throws ExecutionException, CancelledException {
        String path = args[0];
        ConfigurationValue<?> configValue = getByPath(path);
        if (configValue == null)
            throw new ExecutionException(plugin, Message.NOT_A_CONFIGURATION_VALUE.with(Placeholder.INPUT).as(path));

        String input = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));

        try {
            new PluginConfigureCommandEvent(sender, configValue, input).fire();
        } catch (CancelledException e) {
            if (Permission.BYPASS_EXTERNAL_PLUGINS.notOwnedBy(sender))
                throw e;
        }

        String previousValue = configValue.toString();

        if (!input.isEmpty()) {
            try {
                configValue.parseAndSet(input);
            } catch (ArgumentParseException e) {
                throw new ExecutionException(e.getLocalizedMessage());
            }
        } else
            configValue.reset();

        String newValue = configValue.toString();

        plugin.debugf("%s has set %s from %s to %s", sender.getName(), configValue.getPath(), previousValue, newValue);
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
            return plugin.config().stream()
                    .map(ConfigurationValue::getPath)
                    .filter(path -> Utils.containsIgnoreCase(path, args[0]))
                    .sorted()
                    .collect(Collectors.toList());
        ConfigurationValue<?> configValue = getByPath(args[0]);
        if (configValue == null)
            return Collections.emptyList();
        return configValue.getTabCompletions(player, args);
    }

    private ConfigurationValue<?> getByPath(String path) {
        return plugin.config().stream()
                .filter(value -> value.getPath().equalsIgnoreCase(path))
                .findAny()
                .orElse(null);
    }

}
