package com.monst.bankingplugin.config.values.simple;

public class CleanupLogDays extends SimpleInteger {

    public CleanupLogDays() {
        super("cleanup-log-days", 30);
    }

    @Override
    public Integer constrain(Integer i) {
        return Math.abs(i);
    }

}
