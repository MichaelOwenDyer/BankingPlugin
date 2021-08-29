package com.monst.bankingplugin.events.bank;

import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.banking.BankField;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

public class BankConfigureEvent extends SingleBankEvent {

    private final BankField field;
    private final String oldValue;
    private final String newValue;

    public BankConfigureEvent(CommandSender sender, Bank bank, BankField field, String newValue, String oldValue) {
        super(sender, bank);
        this.field = field;
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

    public BankField getField() {
        return field;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }
}
