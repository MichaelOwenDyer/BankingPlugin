package com.monst.bankingplugin.events.account;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AccountInitializedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private int amount;

    public AccountInitializedEvent(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
