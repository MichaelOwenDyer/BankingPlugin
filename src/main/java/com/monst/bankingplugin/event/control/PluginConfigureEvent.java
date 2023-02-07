package com.monst.bankingplugin.event.control;

import com.monst.bankingplugin.configuration.type.ConfigurationValue;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

/**
 * Called when the plugin is configured from in-game.
 */
public class PluginConfigureEvent extends ControlEvent {

    private final ConfigurationValue<?> configValue;
    private final Object newValue;

    public <T> PluginConfigureEvent(ConfigurationValue<T> configValue, T newValue) {
        super(Bukkit.getConsoleSender());
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

    public ConfigurationValue<?> getConfigValue() {
        return configValue;
    }

    public Object getNewValue() {
        return newValue;
    }

}
