package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Bank;
import com.monst.pluginconfiguration.exception.ArgumentParseException;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Represents the default price of creating an account.
 * Can be overridden by {@link Bank}s.
 * @see Bank#getAccountCreationPrice()
 */
public class AccountCreationPrice extends MonetaryConfigurationValue implements BankPolicy<BigDecimal> {

    public final AllowOverride allowOverride;

    public AccountCreationPrice(BankingPlugin plugin) {
        super(plugin, BankPolicy.defaultPath("account-creation-price"), BigDecimal.valueOf(2500));
        this.allowOverride = new AllowOverride(plugin, "account-creation-price");
    }

    @Override
    public String format(BigDecimal value) {
        return plugin.getEconomy().format(value.doubleValue());
    }

    @Override
    public BigDecimal at(Bank bank) {
        if (bank.getAccountCreationPrice() == null) {
            if (plugin.config().stickyDefaults.get())
                bank.setAccountCreationPrice(get());
            return get();
        }
        return allowOverride.get() ? bank.getAccountCreationPrice() : get();
    }

    @Override
    public boolean parseAndSetAt(Bank bank, String input) throws ArgumentParseException {
        if (input == null || input.isEmpty()) {
            bank.setAccountCreationPrice(plugin.config().stickyDefaults.get() ? get() : null);
            return true;
        }
        bank.setAccountCreationPrice(parse(input));
        return allowOverride.get();
    }

    @Override
    public String toStringAt(Bank bank) {
        return format(Optional.ofNullable(bank.getAccountCreationPrice()).orElseGet(this));
    }

}
