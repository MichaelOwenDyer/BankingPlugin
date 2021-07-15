package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.config.values.IConfigInteger;

public class WithdrawalMultiplierDecrement extends OverridableInteger implements IConfigInteger.Absolute {

    public WithdrawalMultiplierDecrement() {
        super("withdrawal-multiplier-decrement", 1);
    }

}
