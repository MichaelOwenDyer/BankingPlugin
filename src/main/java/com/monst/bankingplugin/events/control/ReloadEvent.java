package com.monst.bankingplugin.events.control;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;

/**
 * Called when a player reloads the plugin
 */
public class ReloadEvent extends ControlEvent implements Cancellable {

    private boolean cancelled;

    public ReloadEvent(CommandSender sender) {
        super(sender);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
