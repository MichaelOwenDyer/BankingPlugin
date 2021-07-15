package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.config.values.IConfigDouble;

public class LowBalanceFee extends OverridableDouble implements IConfigDouble.Absolute {

    public LowBalanceFee() {
        super("low-balance-fee", 1000d);
    }

}
