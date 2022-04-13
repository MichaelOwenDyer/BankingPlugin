package com.monst.bankingplugin.event.bank;

import com.monst.bankingplugin.configuration.values.BankPolicy;
import com.monst.bankingplugin.entity.Bank;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

public class BankConfigureEvent extends SingleBankEvent {

    private final BankPolicy<?> policy;
    private final String oldValue;
    private final String newValue;

    public BankConfigureEvent(CommandSender sender, Bank bank, BankPolicy<?> policy, String newValue, String oldValue) {
        super(sender, bank);
        this.policy = policy;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public BankPolicy<?> getPolicy() {
        return policy;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }
}
