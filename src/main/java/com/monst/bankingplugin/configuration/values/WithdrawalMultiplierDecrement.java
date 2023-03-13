package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationPolicy;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.IntegerTransformer;
import com.monst.bankingplugin.entity.Bank;

public class WithdrawalMultiplierDecrement extends ConfigurationPolicy<Integer> {

    public WithdrawalMultiplierDecrement(BankingPlugin plugin) {
        super(plugin, "withdrawal-multiplier-decrement",
                new ConfigurationValue<>(plugin, "default", 1, new IntegerTransformer().atLeast(-1)));
    }
    
    @Override
    protected Integer get(Bank bank) {
        return bank.getWithdrawalMultiplierDecrement();
    }
    
    @Override
    protected void set(Bank bank, Integer value) {
        bank.setWithdrawalMultiplierDecrement(value);
    }
    
}
