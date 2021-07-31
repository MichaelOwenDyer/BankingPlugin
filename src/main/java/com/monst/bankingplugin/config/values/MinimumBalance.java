package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class MinimumBalance extends OverridableValue<Double, Double> implements NativeDouble.Absolute {

    public MinimumBalance(BankingPlugin plugin) {
        super(plugin, "minimum-account-balance", 1000d);
    }

}
