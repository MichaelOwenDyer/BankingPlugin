package com.monst.bankingplugin.events.control;

import com.monst.bankingplugin.config.values.ConfigValue;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;

public class PluginConfigureCommandEvent extends ControlEvent implements Cancellable {

    private boolean cancelled;
    private final ConfigValue<?, ?> configValue;
    private final String newValue;

    public PluginConfigureCommandEvent(CommandSender sender, ConfigValue<?, ?> configValue, String newValue) {
        super(sender);
        this.configValue = configValue;
        this.newValue = newValue;
    }

    public ConfigValue<?, ?> getConfigValue() {
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
