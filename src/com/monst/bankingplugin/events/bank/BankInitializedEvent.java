package com.monst.bankingplugin.events.bank;

import com.monst.bankingplugin.Bank;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Collection;

public class BankInitializedEvent extends MultiBankEvent {

    public BankInitializedEvent(Collection<Bank> banks) {
        super(null, banks);
    }

}
