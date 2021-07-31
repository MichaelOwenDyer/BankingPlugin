package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class InitialInterestDelay extends OverridableValue<Integer, Integer> implements NativeInteger.Absolute {

    public InitialInterestDelay(BankingPlugin plugin) {
        super(plugin, "initial-interest-delay", 0);
    }

}
