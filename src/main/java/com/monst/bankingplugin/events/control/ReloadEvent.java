package com.monst.bankingplugin.events.control;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when the plugin is reloaded.
 */
public class ReloadEvent extends ControlEvent implements Cancellable {

    private boolean cancelled;

    public ReloadEvent(CommandSender sender) {
        super(sender);
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
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
