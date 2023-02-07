package com.monst.bankingplugin.event.control;

import com.monst.bankingplugin.configuration.type.ConfigurationValue;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when a command to configure the plugin is executed.
 */
public class PluginConfigureCommandEvent extends ControlEvent implements Cancellable {

    private boolean cancelled;
    private final ConfigurationValue<?> configValue;
    private final String newValue;

    public PluginConfigureCommandEvent(CommandSender sender, ConfigurationValue<?> configValue, String newValue) {
        super(sender);
        this.configValue = configValue;
        this.newValue = newValue;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public ConfigurationValue<?> getConfigurationValue() {
        return configValue;
    }

    public String getNewValue() {
        return newValue;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

}
