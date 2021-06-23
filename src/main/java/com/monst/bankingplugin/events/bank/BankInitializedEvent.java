package com.monst.bankingplugin.events.bank;

import com.monst.bankingplugin.banking.Bank;
import org.bukkit.Bukkit;

import java.util.Set;

public class BankInitializedEvent extends MultiBankEvent {

    public BankInitializedEvent(Set<Bank> banks) {
        super(Bukkit.getConsoleSender(), banks);
    }

}
