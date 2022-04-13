package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Bank;
import com.monst.pluginconfiguration.exception.ArgumentParseException;

import java.math.BigDecimal;
import java.util.Optional;

public class MinimumBalance extends MonetaryConfigurationValue implements BankPolicy<BigDecimal> {

    public final AllowOverride allowOverride;

    public MinimumBalance(BankingPlugin plugin) {
        super(plugin, BankPolicy.defaultPath("minimum-account-balance"), BigDecimal.valueOf(1000));
        this.allowOverride = new AllowOverride(plugin, "minimum-account-balance");
    }

    @Override
    public String format(BigDecimal value) {
        return plugin.getEconomy().format(value.doubleValue());
    }

    @Override
    public BigDecimal at(Bank bank) {
        if (bank.getMinimumBalance() == null) {
            if (plugin.config().stickyDefaults.get())
                bank.setMinimumBalance(get());
            return get();
        }
        return allowOverride.get() ? bank.getMinimumBalance() : get();
    }

    @Override
    public boolean parseAndSetAt(Bank bank, String input) throws ArgumentParseException {
        if (input == null || input.isEmpty()) {
            bank.setMinimumBalance(plugin.config().stickyDefaults.get() ? get() : null);
            return true;
        }
        bank.setMinimumBalance(parse(input));
        return allowOverride.get();
    }

    @Override
    public String toStringAt(Bank bank) {
        return format(Optional.ofNullable(bank.getMinimumBalance()).orElseGet(this));
    }

}
