package com.monst.bankingplugin.config.values;

public class MaximumBankVolume extends ConfigValue<Integer, Integer> implements NativeInteger.Absolute {

    public MaximumBankVolume() {
        super("bank-size-limits.maximum", 100000);
    }

}
