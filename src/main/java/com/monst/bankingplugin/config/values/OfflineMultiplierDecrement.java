package com.monst.bankingplugin.config.values;

public class OfflineMultiplierDecrement extends OverridableValue<Integer, Integer> implements NativeInteger.Absolute {

    public OfflineMultiplierDecrement() {
        super("offline-multiplier-decrement", 0);
    }

}
