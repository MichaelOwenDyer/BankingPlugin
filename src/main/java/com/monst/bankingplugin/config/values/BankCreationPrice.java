package com.monst.bankingplugin.config.values;

public class BankCreationPrice extends ConfigValue<Double, Double> implements NativeDouble.Absolute {

    public BankCreationPrice() {
        super("bank-creation-price", 100000d);
    }

}
