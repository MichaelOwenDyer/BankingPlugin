package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.configuration.type.BooleanConfigurationValue;
import com.monst.bankingplugin.entity.Bank;

import java.util.Optional;

public class PayOnLowBalance extends BooleanConfigurationValue implements BankPolicy<Boolean> {

    private final AllowOverride allowOverride;

    public PayOnLowBalance(BankingPlugin plugin) {
        super(plugin, BankPolicy.defaultPath("pay-interest-on-low-balance"), true);
        this.allowOverride = new AllowOverride(plugin, "pay-interest-on-low-balance");
    }

    @Override
    public Boolean at(Bank bank) {
        if (bank.paysOnLowBalance() == null) {
            if (plugin.config().stickyDefaults.get())
                bank.setPayOnLowBalance(get());
            return get();
        }
        return allowOverride.get() ? bank.paysOnLowBalance() : get();
    }

    @Override
    public boolean parseAndSetAt(Bank bank, String input) throws ArgumentParseException {
        if (input == null || input.isEmpty()) {
            bank.setPayOnLowBalance(plugin.config().stickyDefaults.get() ? get() : null);
            return true;
        }
        bank.setPayOnLowBalance(parse(input));
        return allowOverride.get();
    }

    @Override
    public String toStringAt(Bank bank) {
        return format(Optional.ofNullable(bank.paysOnLowBalance()).orElseGet(this));
    }
    
    @Override
    public AllowOverride getAllowOverride() {
        return allowOverride;
    }
    
}
