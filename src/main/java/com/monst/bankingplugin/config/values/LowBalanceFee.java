package com.monst.bankingplugin.config.values;

public class LowBalanceFee extends OverridableValue<Double, Double> implements NativeDouble.Absolute {

    public LowBalanceFee() {
        super("low-balance-fee", 1000d);
    }

}
