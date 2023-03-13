package com.monst.bankingplugin.event.control;

import com.monst.bankingplugin.configuration.ConfigurationValue;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

/**
 * Called when the plugin is configured from in-game.
 */
public class PluginConfigureEvent<T> extends ControlEvent {

    private final ConfigurationValue<T> configValue;
    private final T newValue;

    public PluginConfigureEvent(ConfigurationValue<T> configValue) {
        super(Bukkit.getConsoleSender());
        this.configValue = configValue;
        this.newValue = configValue.get();
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public ConfigurationValue<T> getConfigValue() {
        return configValue;
    }

    public T getNewValue() {
        return newValue;
    }

}
