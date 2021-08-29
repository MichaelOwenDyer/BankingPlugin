package com.monst.bankingplugin.events.control;

import com.monst.bankingplugin.config.values.ConfigValue;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

public class PluginConfigureEvent extends ControlEvent {

    private final ConfigValue<?, ?> configValue;
    private final Object newValue;

    public <T> PluginConfigureEvent(ConfigValue<?, T> configValue, T newValue) {
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

    public ConfigValue<?, ?> getConfigValue() {
        return configValue;
    }

    public Object getNewValue() {
        return newValue;
    }

}
