package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.MoneyTransformer;

import java.math.BigDecimal;

/**
 * Represents the price of creating a bank.
 */
public class BankCreationPrice extends ConfigurationValue<BigDecimal> {

    public BankCreationPrice(BankingPlugin plugin) {
        super(plugin, "bank-creation-price", BigDecimal.valueOf(100000), new MoneyTransformer(plugin));
    }

}
