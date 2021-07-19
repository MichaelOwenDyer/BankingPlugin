package com.monst.bankingplugin.config.values;

public class CleanupLogDays extends ConfigValue<Integer, Integer> implements NativeInteger.Absolute {

    public CleanupLogDays() {
        super("cleanup-log-days", 30);
    }

}
