package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Bank;
import com.monst.pluginconfiguration.exception.ArgumentParseException;

import java.math.BigDecimal;
import java.util.Optional;

public class LowBalanceFee extends MonetaryConfigurationValue implements BankPolicy<BigDecimal> {

    public final AllowOverride allowOverride;

    public LowBalanceFee(BankingPlugin plugin) {
        super(plugin, BankPolicy.defaultPath("low-balance-fee"), BigDecimal.valueOf(1000));
        this.allowOverride = new AllowOverride(plugin, "low-balance-fee");
    }

    @Override
    public String format(BigDecimal value) {
        return plugin.getEconomy().format(value.doubleValue());
    }

    @Override
    public BigDecimal at(Bank bank) {
        if (bank.getLowBalanceFee() == null) {
            if (plugin.config().stickyDefaults.get())
                bank.setLowBalanceFee(get());
            return get();
        }
        return allowOverride.get() ? bank.getLowBalanceFee() : get();
    }

    @Override
    public boolean parseAndSetAt(Bank bank, String input) throws ArgumentParseException {
        if (input == null || input.isEmpty()) {
            bank.setLowBalanceFee(plugin.config().stickyDefaults.get() ? get() : null);
            return true;
        }
        bank.setLowBalanceFee(parse(input));
        return allowOverride.get();
    }

    @Override
    public String toStringAt(Bank bank) {
        return format(Optional.ofNullable(bank.getLowBalanceFee()).orElseGet(this));
    }

}
