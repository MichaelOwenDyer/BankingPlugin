package com.monst.bankingplugin.config.values;

public class MinimumBankVolume extends ConfigValue<Integer, Integer> implements NativeInteger.Absolute {

    public MinimumBankVolume() {
        super("bank-size-limits.minimum", 125);
    }

}
