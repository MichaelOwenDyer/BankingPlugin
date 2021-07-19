package com.monst.bankingplugin.config.values;

public class WithdrawalMultiplierDecrement extends OverridableInteger implements IConfigInteger.Absolute {

    public WithdrawalMultiplierDecrement() {
        super("withdrawal-multiplier-decrement", 1);
    }

}
