package com.monst.bankingplugin.events.control;

import com.monst.bankingplugin.config.values.ConfigField;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;

public class PluginConfigureEvent extends ControlEvent implements Cancellable {

    private boolean cancelled;
    private final ConfigField field;
    private final Object newValue;

    public <T> PluginConfigureEvent(CommandSender sender, ConfigField field, T newValue) {
        super(sender);
        this.field = field;
        this.newValue = newValue;
    }

    public ConfigField getField() {
        return field;
    }

    public Object getNewValue() {
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
