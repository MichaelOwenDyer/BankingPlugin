package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationPolicy;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.MoneyTransformer;
import com.monst.bankingplugin.entity.Bank;

import java.math.BigDecimal;

public class MinimumBalance extends ConfigurationPolicy<BigDecimal> {

    public MinimumBalance(BankingPlugin plugin) {
        super(plugin, "minimum-account-balance",
                new ConfigurationValue<>(plugin, "default", BigDecimal.valueOf(1000), new MoneyTransformer(plugin)));
    }
    
    @Override
    protected BigDecimal get(Bank bank) {
        return bank.getMinimumBalance();
    }
    
    @Override
    protected void set(Bank bank, BigDecimal value) {
        bank.setMinimumBalance(value);
    }
    
}
