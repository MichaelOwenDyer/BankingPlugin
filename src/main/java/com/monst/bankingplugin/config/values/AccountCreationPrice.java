package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class AccountCreationPrice extends OverridableValue<Double, Double> implements NativeDouble.Absolute {

    public AccountCreationPrice(BankingPlugin plugin) {
        super(plugin, "account-creation-price", 2500d);
    }

}
