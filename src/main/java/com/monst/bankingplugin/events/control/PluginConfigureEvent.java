package com.monst.bankingplugin.events.control;

import com.monst.bankingplugin.config.values.ConfigValue;
import org.bukkit.Bukkit;

public class PluginConfigureEvent extends ControlEvent {

    private final ConfigValue<?> configValue;
    private final Object newValue;

    public <T> PluginConfigureEvent(ConfigValue<T> configValue, T newValue) {
        super(Bukkit.getConsoleSender());
        this.configValue = configValue;
        this.newValue = newValue;
    }

    public ConfigValue<?> getConfigValue() {
        return configValue;
    }

    public Object getNewValue() {
        return newValue;
    }

}
