package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationPolicy;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.IntegerTransformer;
import com.monst.bankingplugin.entity.Bank;

public class PlayerAccountPerBankLimit extends ConfigurationPolicy<Integer> {

    public PlayerAccountPerBankLimit(BankingPlugin plugin) {
        super(plugin, "player-account-per-bank-limit",
                new ConfigurationValue<>(plugin, "default", 0, new IntegerTransformer().atLeast(-1)));
    }
    
    @Override
    protected Integer get(Bank bank) {
        return bank.getPlayerBankAccountLimit();
    }
    
    @Override
    protected void set(Bank bank, Integer value) {
        bank.setPlayerBankAccountLimit(value);
    }
    
}
