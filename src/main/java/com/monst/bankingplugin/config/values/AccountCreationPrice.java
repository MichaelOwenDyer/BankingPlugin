package com.monst.bankingplugin.config.values;

public class AccountCreationPrice extends OverridableValue<Double, Double> implements NativeDouble.Absolute {

    public AccountCreationPrice() {
        super("account-creation-price", 2500d);
    }

}
