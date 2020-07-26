package com.monst.bankingplugin.events.bank;

import com.monst.bankingplugin.Bank;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Collection;

public class BankInitializedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Collection<Bank> banks;

    public BankInitializedEvent(Collection<Bank> banks) {
        this.banks = banks;
    }

    public Collection<Bank> getBanks() {
        return banks;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
