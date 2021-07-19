package com.monst.bankingplugin.config.values;

public class WithdrawalMultiplierDecrement extends OverridableValue<Integer, Integer> implements NativeInteger.Absolute {

    public WithdrawalMultiplierDecrement() {
        super("withdrawal-multiplier-decrement", 1);
    }

}
