package com.monst.bankingplugin.config.values.overridable;

public class LowBalanceFee extends OverridableDouble {

    public LowBalanceFee() {
        super("low-balance-fee", 1000d);
    }

}
