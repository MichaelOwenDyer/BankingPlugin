package com.monst.bankingplugin.events.bank;

import com.monst.bankingplugin.banking.Bank;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import java.util.Set;

public class BankInitializedEvent extends MultiBankEvent {

    public BankInitializedEvent(Set<Bank> banks) {
        super(Bukkit.getConsoleSender(), banks);
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
