package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationPolicy;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.MoneyTransformer;
import com.monst.bankingplugin.entity.Bank;

import java.math.BigDecimal;

public class LowBalanceFee extends ConfigurationPolicy<BigDecimal> {

    public LowBalanceFee(BankingPlugin plugin) {
        super(plugin, "low-balance-fee",
                new ConfigurationValue<>(plugin, "default", BigDecimal.valueOf(1000), new MoneyTransformer(plugin)));
    }
    
    @Override
    protected BigDecimal get(Bank bank) {
        return bank.getLowBalanceFee();
    }
    
    @Override
    protected void set(Bank bank, BigDecimal value) {
        bank.setLowBalanceFee(value);
    }
    
}
