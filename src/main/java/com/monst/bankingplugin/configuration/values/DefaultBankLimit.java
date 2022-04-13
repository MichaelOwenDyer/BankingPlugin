package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.pluginconfiguration.validation.Bound;

public class DefaultBankLimit extends IntegerConfigurationValue {

    public DefaultBankLimit(BankingPlugin plugin) {
        super(plugin, "default-limits.bank", 1);
    }

    @Override
    protected Bound<Integer> getBound() {
        return Bound.atLeast(-1);
    }

}
