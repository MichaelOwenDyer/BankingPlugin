package com.monst.bankingplugin.events.bank;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.banking.bank.BankField;
import org.bukkit.command.CommandSender;

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
