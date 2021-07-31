package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class MinimumBankVolume extends ConfigValue<Integer, Integer> implements NativeInteger.Absolute {

    public MinimumBankVolume(BankingPlugin plugin) {
        super(plugin, "bank-size-limits.minimum", 125);
    }

}
