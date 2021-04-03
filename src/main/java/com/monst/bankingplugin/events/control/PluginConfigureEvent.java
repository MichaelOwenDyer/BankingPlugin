package com.monst.bankingplugin.events.control;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.values.ConfigField;
import org.bukkit.Bukkit;

public class PluginConfigureEvent extends ControlEvent {

    private final ConfigField field;
    private final Object newValue;

    public PluginConfigureEvent(BankingPlugin plugin, ConfigField field, Object newValue) {
        super(plugin, Bukkit.getConsoleSender());
        this.field = field;
        this.newValue = newValue;
    }

    public ConfigField getField() {
        return field;
    }

    public Object getNewValue() {
        return newValue;
    }

}
