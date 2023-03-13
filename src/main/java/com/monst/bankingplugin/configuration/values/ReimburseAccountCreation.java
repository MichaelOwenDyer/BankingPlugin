package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationPolicy;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.BooleanTransformer;
import com.monst.bankingplugin.entity.Bank;

public class ReimburseAccountCreation extends ConfigurationPolicy<Boolean> {

    public ReimburseAccountCreation(BankingPlugin plugin) {
        super(plugin, "reimburse-account-creation",
                new ConfigurationValue<>(plugin, "default", false, new BooleanTransformer()));
    }
    
    @Override
    protected Boolean get(Bank bank) {
        return bank.reimbursesAccountCreation();
    }
    
    @Override
    protected void set(Bank bank, Boolean value) {
        bank.setReimburseAccountCreation(value);
    }

}
