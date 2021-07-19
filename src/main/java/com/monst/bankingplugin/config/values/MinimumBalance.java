package com.monst.bankingplugin.config.values;

public class MinimumBalance extends OverridableValue<Double, Double> implements NativeDouble.Absolute {

    public MinimumBalance() {
        super("minimum-account-balance", 1000d);
    }

}
