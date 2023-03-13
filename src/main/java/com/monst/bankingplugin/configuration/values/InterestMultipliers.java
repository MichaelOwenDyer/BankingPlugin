package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationPolicy;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.IntegerTransformer;
import com.monst.bankingplugin.configuration.validation.Bound;
import com.monst.bankingplugin.entity.Bank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A non-empty list of positive integers. Defaults to [1]
 */
public class InterestMultipliers extends ConfigurationPolicy<List<Integer>> {

    public InterestMultipliers(BankingPlugin plugin) {
        super(plugin, "interest-multipliers", new ConfigurationValue<>(plugin, "default", Collections.singletonList(1),
                new IntegerTransformer()
                        .absolute()
                        .<List<Integer>>collect(ArrayList::new)
                        .bounded(Bound.disallowing(List::isEmpty, list -> Collections.singletonList(1)))));
    }
    
    @Override
    protected List<Integer> get(Bank bank) {
        return bank.getInterestMultipliers();
    }
    
    @Override
    protected void set(Bank bank, List<Integer> value) {
        bank.setInterestMultipliers(value);
    }
    
}
