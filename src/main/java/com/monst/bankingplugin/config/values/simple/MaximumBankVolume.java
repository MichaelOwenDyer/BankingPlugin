package com.monst.bankingplugin.config.values.simple;

public class MaximumBankVolume extends SimpleInteger {

    public MaximumBankVolume() {
        super("bank-size-limits.maximum", 100000);
    }

    @Override
    public Integer constrain(Integer i) {
        return Math.max(i, 0);
    }

}
