package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.config.values.IConfigInteger;

public class CleanupLogDays extends SimpleInteger implements IConfigInteger.Absolute {

    public CleanupLogDays() {
        super("cleanup-log-days", 30);
    }

}
