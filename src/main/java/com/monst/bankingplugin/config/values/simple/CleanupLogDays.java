package com.monst.bankingplugin.config.values.simple;

import java.util.function.Function;

public class CleanupLogDays extends SimpleInteger {

    public CleanupLogDays() {
        super("cleanup-log-days", 30);
    }

    @Override
    Function<Integer, Integer> getConstraint() {
        return Math::abs;
    }

}
