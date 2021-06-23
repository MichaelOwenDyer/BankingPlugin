package com.monst.bankingplugin.events.bank;

import com.monst.bankingplugin.banking.Bank;
import org.bukkit.command.CommandSender;

import java.util.Collection;

public abstract class MultiBankEvent extends BankEvent {

    private final Collection<Bank> banks;

    public MultiBankEvent(CommandSender sender, Collection<Bank> banks) {
        super(sender);
        this.banks = banks;
    }

    public Collection<Bank> getBanks() {
        return banks;
    }

}
