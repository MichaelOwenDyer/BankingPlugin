package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationPolicy;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.LocalTimeTransformer;
import com.monst.bankingplugin.entity.Bank;

import java.time.LocalTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * An ordered set of times of the day.
 */
public class InterestPayoutTimes extends ConfigurationPolicy<Set<LocalTime>> {
    
    public InterestPayoutTimes(BankingPlugin plugin) {
        super(plugin, "interest-payout-times", new Default(plugin));
    }
    
    private static class Default extends ConfigurationValue<Set<LocalTime>> {
    
        private Default(BankingPlugin plugin) {
            super(plugin, "default", Collections.emptySet(),
                    new LocalTimeTransformer().collect(LinkedHashSet::new));
        }
    
        @Override
        public void afterSet() {
            plugin.getSchedulerService().scheduleAll();
        }
    
    }
    
    @Override
    protected Set<LocalTime> get(Bank bank) {
        return bank.getInterestPayoutTimes();
    }
    
    @Override
    protected void set(Bank bank, Set<LocalTime> value) {
        bank.setInterestPayoutTimes(value);
    }
    
}
