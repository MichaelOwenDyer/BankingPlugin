package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

import java.math.BigDecimal;

public class BankCreationPrice extends ConfigValue<Double, BigDecimal> implements NonNativeBigDecimal.Absolute {

    public BankCreationPrice(BankingPlugin plugin) {
        super(plugin, "bank-creation-price", BigDecimal.valueOf(100000));
    }

}
