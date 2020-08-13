package com.monst.bankingplugin.events.account;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class AccountEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final CommandSender executor;

    public AccountEvent(CommandSender executor) {
        this.executor = executor;
    }

    public CommandSender getExecutor() {
        return executor;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
