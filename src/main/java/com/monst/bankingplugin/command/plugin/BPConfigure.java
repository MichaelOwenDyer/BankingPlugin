package com.monst.bankingplugin.command.plugin;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.Permission;
import com.monst.bankingplugin.command.Permissions;
import com.monst.bankingplugin.command.SubCommand;
import com.monst.bankingplugin.configuration.ConfigurationBranch;
import com.monst.bankingplugin.configuration.ConfigurationNode;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.event.control.PluginConfigureCommandEvent;
import com.monst.bankingplugin.event.control.PluginConfigureEvent;
import com.monst.bankingplugin.exception.CommandExecutionException;
import com.monst.bankingplugin.exception.EventCancelledException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
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
        int arg = 0;
        ConfigurationNode targetNode = plugin.config();
        while (targetNode instanceof ConfigurationBranch && arg < args.length)
            targetNode = ((ConfigurationBranch) targetNode).getChild(args[arg++]);
        String path = String.join(".", Arrays.copyOfRange(args, 0, arg));
        if (!(targetNode instanceof ConfigurationValue))
            throw err(Message.NOT_A_CONFIGURATION_VALUE.with(Placeholder.INPUT).as(path));
        
        ConfigurationValue<?> configValue = (ConfigurationValue<?>) targetNode;
    
        String input = arg >= args.length ? null : String.join(" ", Arrays.copyOfRange(args, arg, args.length));

        try {
            new PluginConfigureCommandEvent(sender, configValue, input).fire();
        } catch (EventCancelledException e) {
            if (Permissions.BYPASS_EXTERNAL_PLUGINS.notOwnedBy(sender))
                throw e;
        }

        String previousValue = configValue.toString();

        try {
            configValue.feed(input);
        } catch (ArgumentParseException e) {
            throw err(e.getTranslatableMessage());
        }
        
        new PluginConfigureEvent<>(configValue).fire();

        String newValue = configValue.toString();

        plugin.debug("%s has configured %s from %s to %s",
                sender.getName(), configValue.getKey(), previousValue, newValue);
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
        return getTabCompletions(plugin.config(), player, args, 0);
    }
    
    private List<String> getTabCompletions(ConfigurationBranch branch, Player player, String[] args, int arg) {
        if (arg >= args.length - 1)
            return branch.getChildren().keySet().stream()
                    .filter(name -> containsIgnoreCase(name, args[args.length - 1]))
                    .collect(Collectors.toList());
        ConfigurationNode node = branch.getChild(args[arg]);
        if (node instanceof ConfigurationBranch)
            return getTabCompletions((ConfigurationBranch) node, player, args, arg + 1);
        if (node instanceof ConfigurationValue)
            return ((ConfigurationValue<?>) node).getTabCompletions(player, args);
        return Collections.emptyList();
    }
    
}
