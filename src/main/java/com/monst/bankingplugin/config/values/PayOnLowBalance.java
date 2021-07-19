package com.monst.bankingplugin.config.values;

public class PayOnLowBalance extends OverridableBoolean {

    public PayOnLowBalance() {
        super("pay-interest-on-low-balance", true);
    }

}
