package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Bank;
import com.monst.pluginconfiguration.exception.ArgumentParseException;
import com.monst.pluginconfiguration.validation.Bound;

import java.util.Optional;

/**
 * The number of times in a row that an account can pay out interest while all account holders are offline.
 * -1 -> no limit
 */
public class AllowedOfflinePayouts extends IntegerConfigurationValue implements BankPolicy<Integer> {

    public final AllowOverride allowOverride;

    public AllowedOfflinePayouts(BankingPlugin plugin) {
        super(plugin, BankPolicy.defaultPath("allowed-offline-payouts"), 1);
        this.allowOverride = new AllowOverride(plugin, "allowed-offline-payouts");
    }

    @Override
    protected Bound<Integer> getBound() {
        return Bound.atLeast(-1);
    }

    @Override
    public Integer at(Bank bank) {
        if (bank.getAllowedOfflinePayouts() == null) {
            if (plugin.config().stickyDefaults.get())
                bank.setAllowedOfflinePayouts(get());
            return get();
        }
        return allowOverride.get() ? bank.getAllowedOfflinePayouts() : get();
    }

    @Override
    public boolean parseAndSetAt(Bank bank, String input) throws ArgumentParseException {
        if (input == null || input.isEmpty()) {
            bank.setAllowedOfflinePayouts(plugin.config().stickyDefaults.get() ? get() : null);
            return true;
        }
        bank.setAllowedOfflinePayouts(parse(input));
        return allowOverride.get();
    }

    @Override
    public String toStringAt(Bank bank) {
        return format(Optional.ofNullable(bank.getAllowedOfflinePayouts()).orElseGet(this));
    }

}
