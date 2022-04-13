package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.pluginconfiguration.validation.Bound;

public class MaximumBankVolume extends IntegerConfigurationValue {

    public MaximumBankVolume(BankingPlugin plugin) {
        super(plugin, "bank-size-limits.maximum", 100000);
    }

    @Override
    protected Bound<Integer> getBound() {
        return Bound.atLeast(-1);
    }

}
