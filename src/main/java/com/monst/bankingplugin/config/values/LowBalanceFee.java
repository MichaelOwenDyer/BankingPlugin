package com.monst.bankingplugin.config.values;

public class LowBalanceFee extends OverridableDouble implements IConfigDouble.Absolute {

    public LowBalanceFee() {
        super("low-balance-fee", 1000d);
    }

}
