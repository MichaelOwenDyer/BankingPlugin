package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class WithdrawalMultiplierDecrement extends OverridableValue<Integer, Integer> implements NativeInteger.Absolute {

    public WithdrawalMultiplierDecrement(BankingPlugin plugin) {
        super(plugin, "withdrawal-multiplier-decrement", 1);
    }

}
