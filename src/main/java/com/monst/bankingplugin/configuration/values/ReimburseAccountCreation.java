package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.configuration.type.BooleanConfigurationValue;
import com.monst.bankingplugin.entity.Bank;

import java.util.Optional;

public class ReimburseAccountCreation extends BooleanConfigurationValue implements BankPolicy<Boolean> {

    private final AllowOverride allowOverride;

    public ReimburseAccountCreation(BankingPlugin plugin) {
        super(plugin, BankPolicy.defaultPath("reimburse-account-creation"), false);
        this.allowOverride = new AllowOverride(plugin, "reimburse-account-creation");
    }

    @Override
    public Boolean at(Bank bank) {
        if (bank.reimbursesAccountCreation() == null) {
            if (plugin.config().stickyDefaults.get())
                bank.setReimburseAccountCreation(get());
            return get();
        }
        return allowOverride.get() ? bank.reimbursesAccountCreation() : get();
    }

    @Override
    public boolean parseAndSetAt(Bank bank, String input) throws ArgumentParseException {
        if (input == null || input.isEmpty()) {
            bank.setReimburseAccountCreation(plugin.config().stickyDefaults.get() ? get() : null);
            return true;
        }
        bank.setReimburseAccountCreation(parse(input));
        return allowOverride.get();
    }

    @Override
    public String toStringAt(Bank bank) {
        return format(Optional.ofNullable(bank.reimbursesAccountCreation()).orElseGet(this));
    }
    
    @Override
    public AllowOverride getAllowOverride() {
        return allowOverride;
    }
    
}
