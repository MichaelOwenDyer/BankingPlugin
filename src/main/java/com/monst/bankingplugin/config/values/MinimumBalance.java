package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

import java.math.BigDecimal;

public class MinimumBalance extends OverridableValue<Double, BigDecimal> implements NonNativeBigDecimal.Absolute {

    public MinimumBalance(BankingPlugin plugin) {
        super(plugin, "minimum-account-balance", BigDecimal.valueOf(1000));
    }

}
