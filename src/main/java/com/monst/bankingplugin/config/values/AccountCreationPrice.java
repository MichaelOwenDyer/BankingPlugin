package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

import java.math.BigDecimal;

public class AccountCreationPrice extends OverridableValue<Double, BigDecimal> implements NonNativeBigDecimal.Absolute {

    public AccountCreationPrice(BankingPlugin plugin) {
        super(plugin, "account-creation-price", BigDecimal.valueOf(2500));
    }

}
