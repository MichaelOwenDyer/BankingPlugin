package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class LowBalanceFee extends OverridableValue<Double, Double> implements NativeDouble.Absolute {

    public LowBalanceFee(BankingPlugin plugin) {
        super(plugin, "low-balance-fee", 1000d);
    }

}
