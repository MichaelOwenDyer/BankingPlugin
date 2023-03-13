package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.IntegerTransformer;

import java.util.Optional;

/**
 * The number of days old that database logs are allowed to become before they are automatically deleted.
 * This value is optional and will be ignored if not set.
 */
public class CleanupLogDays extends ConfigurationValue<Optional<Integer>> {

    public CleanupLogDays(BankingPlugin plugin) {
        super(plugin, "cleanup-log-days", Optional.empty(),
                new IntegerTransformer().absolute().optional());
    }

}
