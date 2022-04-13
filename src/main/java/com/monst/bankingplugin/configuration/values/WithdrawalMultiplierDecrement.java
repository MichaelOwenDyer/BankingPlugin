package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Bank;
import com.monst.pluginconfiguration.exception.ArgumentParseException;
import com.monst.pluginconfiguration.validation.Bound;

import java.util.Optional;

public class WithdrawalMultiplierDecrement extends IntegerConfigurationValue implements BankPolicy<Integer> {

    public final AllowOverride allowOverride;

    public WithdrawalMultiplierDecrement(BankingPlugin plugin) {
        super(plugin, BankPolicy.defaultPath("withdrawal-multiplier-decrement"), 1);
        this.allowOverride = new AllowOverride(plugin, "withdrawal-multiplier-decrement");
    }

    @Override
    protected Bound<Integer> getBound() {
        return Bound.atLeast(-1);
    }

    @Override
    public Integer at(Bank bank) {
        if (bank.getWithdrawalMultiplierDecrement() == null) {
            if (plugin.config().stickyDefaults.get())
                bank.setWithdrawalMultiplierDecrement(get());
            return get();
        }
        return allowOverride.get() ? bank.getWithdrawalMultiplierDecrement() : get();
    }

    @Override
    public boolean parseAndSetAt(Bank bank, String input) throws ArgumentParseException {
        if (input == null || input.isEmpty()) {
            bank.setWithdrawalMultiplierDecrement(plugin.config().stickyDefaults.get() ? get() : null);
            return true;
        }
        bank.setWithdrawalMultiplierDecrement(parse(input));
        return allowOverride.get();
    }

    @Override
    public String toStringAt(Bank bank) {
        return format(Optional.ofNullable(bank.getWithdrawalMultiplierDecrement()).orElseGet(this));
    }

}
