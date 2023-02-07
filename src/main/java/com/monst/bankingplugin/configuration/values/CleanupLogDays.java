package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.type.IntegerConfigurationValue;
import com.monst.bankingplugin.configuration.validation.Bound;

/**
 * The number of days old that database logs are allowed to become before they are automatically deleted.
 * -1 -> never delete
 */
public class CleanupLogDays extends IntegerConfigurationValue {

    public CleanupLogDays(BankingPlugin plugin) {
        super(plugin, "cleanup-log-days", -1);
    }

    @Override
    protected Bound<Integer> getBound() {
        return Bound.atLeast(-1);
    }

}
