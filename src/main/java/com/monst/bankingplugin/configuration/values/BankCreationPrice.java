package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;

import java.math.BigDecimal;

/**
 * Represents the price of creating a bank.
 */
public class BankCreationPrice extends MonetaryConfigurationValue {

    public BankCreationPrice(BankingPlugin plugin) {
        super(plugin, "bank-creation-price", BigDecimal.valueOf(100000));
    }

    @Override
    public String format(BigDecimal value) {
        return plugin.getEconomy().format(value.doubleValue());
    }

}
