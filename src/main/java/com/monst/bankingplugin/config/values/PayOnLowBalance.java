package com.monst.bankingplugin.config.values;

public class PayOnLowBalance extends OverridableValue<Boolean, Boolean> implements NativeBoolean {

    public PayOnLowBalance() {
        super("pay-interest-on-low-balance", true);
    }

}
