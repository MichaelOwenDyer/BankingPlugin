package com.monst.bankingplugin.events.bank;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BankInitializedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final int amount;

    public BankInitializedEvent(int amount) {
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
