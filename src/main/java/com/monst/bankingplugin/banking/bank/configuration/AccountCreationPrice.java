package com.monst.bankingplugin.banking.bank.configuration;

import com.monst.bankingplugin.config.Config;

public class AccountCreationPrice extends DoubleConfigurationOption {

    public AccountCreationPrice() {
        super();
    }

    public AccountCreationPrice(Double value) {
        super(value);
    }

    @Override
    protected Config.ConfigPair<Double> getConfigPair() {
        return Config.getAccountCreationPrice();
    }

}
