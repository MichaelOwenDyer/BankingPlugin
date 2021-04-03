package com.monst.bankingplugin.events.bank;

import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.config.values.ConfigField;
import org.bukkit.command.CommandSender;

public class BankConfigureEvent extends SingleBankEvent {

    private final ConfigField field;
    private final String oldValue;
    private final String newValue;

    public BankConfigureEvent(CommandSender sender, Bank bank, ConfigField field, String oldValue, String newValue) {
        super(sender, bank);
        this.field = field;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public ConfigField getField() {
        return field;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }
}
