package com.monst.bankingplugin.event.bank;

import com.monst.bankingplugin.entity.Bank;
import org.bukkit.command.CommandSender;

import java.util.Collection;

/**
 * An event involving multiple {@link Bank}s.
 */
public abstract class MultiBankEvent extends BankEvent {

    private final Collection<Bank> banks;

    protected MultiBankEvent(CommandSender sender, Collection<Bank> banks) {
        super(sender);
        this.banks = banks;
    }

    public Collection<Bank> getBanks() {
        return banks;
    }

}
