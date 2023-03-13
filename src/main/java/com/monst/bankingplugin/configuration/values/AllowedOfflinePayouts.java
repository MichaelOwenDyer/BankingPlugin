package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationPolicy;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.IntegerTransformer;
import com.monst.bankingplugin.entity.Bank;

/**
 * The number of times in a row that an account can pay out interest while all account holders are offline.
 * -1 -> no limit
 */
public class AllowedOfflinePayouts extends ConfigurationPolicy<Integer> {

    public AllowedOfflinePayouts(BankingPlugin plugin) {
        super(plugin, "allowed-offline-payouts",
                new ConfigurationValue<>(plugin, "default", 1, new IntegerTransformer().atLeast(-1)));
    }
    
    @Override
    protected Integer get(Bank bank) {
        return bank.getAllowedOfflinePayouts();
    }
    
    @Override
    protected void set(Bank bank, Integer value) {
        bank.setAllowedOfflinePayouts(value);
    }
    
}
