package com.monst.bankingplugin.config.values.simple;

import java.util.function.Function;

public class CleanupLogDays extends ConfigInteger {

    public CleanupLogDays() {
        super("cleanup-log-days", 30);
    }

    @Override
    public Function<Integer, Integer> getConstraint() {
        return Math::abs;
    }

}
