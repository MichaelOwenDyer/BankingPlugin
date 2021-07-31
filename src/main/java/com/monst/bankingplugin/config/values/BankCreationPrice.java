package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class BankCreationPrice extends ConfigValue<Double, Double> implements NativeDouble.Absolute {

    public BankCreationPrice(BankingPlugin plugin) {
        super(plugin, "bank-creation-price", 100000d);
    }

}
