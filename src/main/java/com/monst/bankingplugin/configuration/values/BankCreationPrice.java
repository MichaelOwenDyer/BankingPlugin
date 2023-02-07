package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.type.MonetaryConfigurationValue;

import java.math.BigDecimal;

/**
 * Represents the price of creating a bank.
 */
public class BankCreationPrice extends MonetaryConfigurationValue {

    public BankCreationPrice(BankingPlugin plugin) {
        super(plugin, "bank-creation-price", BigDecimal.valueOf(100000));
    }

}
