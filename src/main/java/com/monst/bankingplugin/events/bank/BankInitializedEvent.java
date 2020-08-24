package com.monst.bankingplugin.events.bank;

import com.monst.bankingplugin.banking.bank.Bank;
import org.bukkit.Bukkit;

import java.util.Collection;

public class BankInitializedEvent extends MultiBankEvent {

    public BankInitializedEvent(Collection<Bank> banks) {
        super(Bukkit.getConsoleSender(), banks);
    }

}
