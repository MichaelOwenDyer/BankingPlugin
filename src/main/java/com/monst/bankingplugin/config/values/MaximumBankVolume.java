package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class MaximumBankVolume extends ConfigValue<Integer, Integer> implements NativeInteger.Absolute {

    public MaximumBankVolume(BankingPlugin plugin) {
        super(plugin, "bank-size-limits.maximum", 100000);
    }

}
