package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationPolicy;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.BooleanTransformer;
import com.monst.bankingplugin.entity.Bank;

public class PayOnLowBalance extends ConfigurationPolicy<Boolean> {

    public PayOnLowBalance(BankingPlugin plugin) {
        super(plugin, "pay-interest-on-low-balance",
                new ConfigurationValue<>(plugin, "default", true, new BooleanTransformer()));
    }
    
    @Override
    protected Boolean get(Bank bank) {
        return bank.paysOnLowBalance();
    }
    
    @Override
    protected void set(Bank bank, Boolean value) {
        bank.setPayOnLowBalance(value);
    }
    
}
