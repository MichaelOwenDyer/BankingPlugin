package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationPolicy;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.MoneyTransformer;
import com.monst.bankingplugin.entity.Bank;

import java.math.BigDecimal;

import static com.monst.bankingplugin.configuration.transform.BigDecimalTransformer.positive;

/**
 * Represents the default price of creating an account.
 * Can be overridden by {@link Bank}s.
 * @see Bank#getAccountCreationPrice()
 */
public class AccountCreationPrice extends ConfigurationPolicy<BigDecimal> {

    public AccountCreationPrice(BankingPlugin plugin) {
        super(plugin, "account-creation-price",
                new ConfigurationValue<>(plugin, "default", BigDecimal.valueOf(2500),
                        new MoneyTransformer(plugin).bounded(positive())));
    }
    
    @Override
    protected BigDecimal get(Bank bank) {
        return bank.getAccountCreationPrice();
    }
    
    @Override
    protected void set(Bank bank, BigDecimal value) {
        bank.setAccountCreationPrice(value);
    }
    
}
