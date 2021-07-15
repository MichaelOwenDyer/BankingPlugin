package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.config.values.IConfigInteger;

public class OfflineMultiplierDecrement extends OverridableInteger implements IConfigInteger.Absolute {

    public OfflineMultiplierDecrement() {
        super("offline-multiplier-decrement", 0);
    }

}
