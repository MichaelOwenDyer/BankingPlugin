package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

import java.math.BigDecimal;

public class LowBalanceFee extends OverridableValue<Double, BigDecimal> implements NonNativeBigDecimal.Absolute {

    public LowBalanceFee(BankingPlugin plugin) {
        super(plugin, "low-balance-fee", BigDecimal.valueOf(1000));
    }

}
