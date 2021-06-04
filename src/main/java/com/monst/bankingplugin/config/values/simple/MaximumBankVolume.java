package com.monst.bankingplugin.config.values.simple;

import java.util.function.Function;

public class MaximumBankVolume extends SimpleInteger {

    public MaximumBankVolume() {
        super("bank-size-limits.maximum", 100000);
    }

    @Override
    public Function<Integer, Integer> getConstraint() {
        return i -> Math.max(i, 0);
    }

}
