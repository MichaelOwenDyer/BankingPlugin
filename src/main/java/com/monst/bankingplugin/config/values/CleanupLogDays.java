package com.monst.bankingplugin.config.values;

public class CleanupLogDays extends SimpleInteger implements IConfigInteger.Absolute {

    public CleanupLogDays() {
        super("cleanup-log-days", 30);
    }

}
