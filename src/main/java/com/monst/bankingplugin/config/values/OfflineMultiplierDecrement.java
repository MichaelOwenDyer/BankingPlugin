package com.monst.bankingplugin.config.values;

public class OfflineMultiplierDecrement extends OverridableInteger implements IConfigInteger.Absolute {

    public OfflineMultiplierDecrement() {
        super("offline-multiplier-decrement", 0);
    }

}
