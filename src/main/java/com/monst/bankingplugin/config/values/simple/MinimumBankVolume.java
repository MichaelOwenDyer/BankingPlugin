package com.monst.bankingplugin.config.values.simple;

import java.util.function.Function;

public class MinimumBankVolume extends SimpleInteger {

    public MinimumBankVolume() {
        super("bank-size-limits.minimum", 125);
    }

    @Override
    public Function<Integer, Integer> getConstraint() {
        return i -> Math.max(i, 0);
    }

}
