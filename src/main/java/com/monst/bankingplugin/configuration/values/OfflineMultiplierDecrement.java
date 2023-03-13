package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationPolicy;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.IntegerTransformer;
import com.monst.bankingplugin.configuration.validation.Bound;
import com.monst.bankingplugin.entity.Bank;

public class OfflineMultiplierDecrement extends ConfigurationPolicy<Integer> {

    public OfflineMultiplierDecrement(BankingPlugin plugin) {
        super(plugin, "offline-multiplier-decrement",
                new ConfigurationValue<>(plugin, "default", 0,
                        new IntegerTransformer().bounded(Bound.atLeast(-1))));
    }
    
    @Override
    protected Integer get(Bank bank) {
        return bank.getOfflineMultiplierDecrement();
    }
    
    @Override
    protected void set(Bank bank, Integer value) {
        bank.setOfflineMultiplierDecrement(value);
    }
    
}
