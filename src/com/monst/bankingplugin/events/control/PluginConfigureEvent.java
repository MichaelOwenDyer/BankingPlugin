package com.monst.bankingplugin.events.control;

import com.monst.bankingplugin.BankingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class PluginConfigureEvent extends ControlEvent {

    private final String field;
    private final String newValue;

    public PluginConfigureEvent(BankingPlugin plugin, String field, String newValue) {
        super(plugin, Bukkit.getConsoleSender());
        this.field = field;
        this.newValue = newValue;
    }

    public String getField() {
        return field;
    }

    public String getNewValue() {
        return newValue;
    }

}
