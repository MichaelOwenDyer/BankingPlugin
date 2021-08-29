package com.monst.bankingplugin.commands.control;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.commands.SubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.config.values.ConfigValue;
import com.monst.bankingplugin.events.control.PluginConfigureCommandEvent;
import com.monst.bankingplugin.exceptions.parse.ArgumentParseException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.utils.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ControlConfigure extends SubCommand {

    ControlConfigure(BankingPlugin plugin) {
		super(plugin, "configure", false);
    }

    @Override
    protected Permission getPermission() {
        return Permission.CONFIG;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_CONFIG;
    }

    @Override
    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION_CONFIG;
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        if (args.length < 1)
            return false;

        String path = args[0];
        ConfigValue<?, ?> configValue = Config.getByPath(path);
        if (configValue == null) {
            sender.sendMessage(Message.NOT_A_CONFIG_VALUE.with(Placeholder.INPUT).as(path).translate());
            return true;
        }

        String input = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));

        PluginConfigureCommandEvent event = new PluginConfigureCommandEvent(sender, configValue, input);
        event.fire();
        if (event.isCancelled() && Permission.BYPASS_EXTERNAL_PLUGINS.notOwnedBy(sender)) {
            plugin.debug("Plugin configure event cancelled");
            return true;
        }

        String previousValue = configValue.getFormatted();

        try {
            configValue.set(input);
        } catch (ArgumentParseException e) {
            sender.sendMessage(e.getLocalizedMessage());
            plugin.debugf("Could not parse argument: \"%s\"", e.getLocalizedMessage());
            return true;
        }

        String newValue = configValue.getFormatted();

        plugin.debugf("%s has set %s from %s to %s", sender.getName(), configValue.getPath(), previousValue, newValue);
        sender.sendMessage(Message.CONFIG_VALUE_SET
                .with(Placeholder.PROPERTY).as(path)
                .and(Placeholder.PREVIOUS_VALUE).as(previousValue)
                .and(Placeholder.VALUE).as(newValue)
                .translate());
        if (!configValue.isHotSwappable())
            sender.sendMessage(Message.RESTART_REQUIRED.with(Placeholder.PROPERTY).as(path).translate());
        configValue.afterSet(sender);
        return true;
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (Permission.CONFIG.notOwnedBy(player))
            return Collections.emptyList();
        if (args.length == 1)
            return Config.matchPath(args[0]);
        ConfigValue<?, ?> configValue = Config.getByPath(args[0]);
        if (configValue == null)
            return Collections.emptyList();
        return configValue.getTabCompletions(player, args);
    }

}
