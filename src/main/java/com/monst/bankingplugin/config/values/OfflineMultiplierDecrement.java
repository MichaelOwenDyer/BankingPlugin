package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class OfflineMultiplierDecrement extends OverridableValue<Integer, Integer> implements NativeInteger.Absolute {

    public OfflineMultiplierDecrement(BankingPlugin plugin) {
        super(plugin, "offline-multiplier-decrement", 0);
    }

}
