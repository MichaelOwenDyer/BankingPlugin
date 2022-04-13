package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.pluginconfiguration.validation.Bound;

public class MinimumBankVolume extends IntegerConfigurationValue {

    public MinimumBankVolume(BankingPlugin plugin) {
        super(plugin, "bank-size-limits.minimum", 125);
    }

    @Override
    protected Bound<Integer> getBound() {
        return Bound.atLeast(-1);
    }

}