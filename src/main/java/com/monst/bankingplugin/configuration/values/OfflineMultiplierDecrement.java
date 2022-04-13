package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Bank;
import com.monst.pluginconfiguration.exception.ArgumentParseException;
import com.monst.pluginconfiguration.validation.Bound;

import java.util.Optional;

public class OfflineMultiplierDecrement extends IntegerConfigurationValue implements BankPolicy<Integer> {

    public final AllowOverride allowOverride;

    public OfflineMultiplierDecrement(BankingPlugin plugin) {
        super(plugin, BankPolicy.defaultPath("offline-multiplier-decrement"), 0);
        this.allowOverride = new AllowOverride(plugin, "offline-multiplier-decrement");
    }

    @Override
    protected Bound<Integer> getBound() {
        return Bound.atLeast(-1);
    }

    @Override
    public Integer at(Bank bank) {
        if (bank.getOfflineMultiplierDecrement() == null) {
            if (plugin.config().stickyDefaults.get())
                bank.setOfflineMultiplierDecrement(get());
            return get();
        }
        return allowOverride.get() ? bank.getOfflineMultiplierDecrement() : get();
    }

    @Override
    public boolean parseAndSetAt(Bank bank, String input) throws ArgumentParseException {
        if (input == null || input.isEmpty()) {
            bank.setOfflineMultiplierDecrement(plugin.config().stickyDefaults.get() ? get() : null);
            return true;
        }
        bank.setOfflineMultiplierDecrement(parse(input));
        return allowOverride.get();
    }

    @Override
    public String toStringAt(Bank bank) {
        return format(Optional.ofNullable(bank.getOfflineMultiplierDecrement()).orElseGet(this));
    }

}
