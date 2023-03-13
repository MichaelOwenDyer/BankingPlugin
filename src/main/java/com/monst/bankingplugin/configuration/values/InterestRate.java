package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationPolicy;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.PercentageTransformer;
import com.monst.bankingplugin.entity.Bank;

import java.math.BigDecimal;

import static com.monst.bankingplugin.configuration.transform.BigDecimalTransformer.positive;
import static com.monst.bankingplugin.configuration.transform.BigDecimalTransformer.scale;

public class InterestRate extends ConfigurationPolicy<BigDecimal> {
    public InterestRate(BankingPlugin plugin) {
        super(plugin, "interest-rate",
                new ConfigurationValue<>(plugin, "default", BigDecimal.ONE.scaleByPowerOfTen(-2),
                        new PercentageTransformer().bounded(positive().and(scale(4)))));
    }
    
    @Override
    protected BigDecimal get(Bank bank) {
        return bank.getInterestRate();
    }
    
    @Override
    protected void set(Bank bank, BigDecimal value) {
        bank.setInterestRate(value);
    }
    
}
