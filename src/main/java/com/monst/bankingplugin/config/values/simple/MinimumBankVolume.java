package com.monst.bankingplugin.config.values.simple;

public class MinimumBankVolume extends SimpleInteger {

    public MinimumBankVolume() {
        super("bank-size-limits.minimum", 125);
    }

    @Override
    public Integer constrain(Integer i) {
        return Math.max(i, 0);
    }

}
