package com.monst.bankingplugin.config.values.overridable;

public class PayOnLowBalance extends OverridableBoolean {

    public PayOnLowBalance() {
        super("pay-interest-on-low-balance", true);
    }

}
