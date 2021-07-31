package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class PayOnLowBalance extends OverridableValue<Boolean, Boolean> implements NativeBoolean {

    public PayOnLowBalance(BankingPlugin plugin) {
        super(plugin, "pay-interest-on-low-balance", true);
    }

}
