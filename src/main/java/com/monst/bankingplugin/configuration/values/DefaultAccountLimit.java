package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.type.IntegerConfigurationValue;
import com.monst.bankingplugin.configuration.validation.Bound;

/**
 * The default limit to the number of accounts a player may own.
 * -1 -> no limit
 */
public class DefaultAccountLimit extends IntegerConfigurationValue {

    public DefaultAccountLimit(BankingPlugin plugin) {
        super(plugin, "default-limits.account", 3);
    }

    @Override
    protected Bound<Integer> getBound() {
        return Bound.atLeast(-1);
    }

}
