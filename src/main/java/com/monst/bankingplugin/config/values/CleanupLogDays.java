package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class CleanupLogDays extends ConfigValue<Integer, Integer> implements NativeInteger.Absolute {

    public CleanupLogDays(BankingPlugin plugin) {
        super(plugin, "cleanup-log-days", 30);
    }

}
