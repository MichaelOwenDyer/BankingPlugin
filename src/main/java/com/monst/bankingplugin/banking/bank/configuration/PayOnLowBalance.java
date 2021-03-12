package com.monst.bankingplugin.banking.bank.configuration;

import com.monst.bankingplugin.config.Config;

public class PayOnLowBalance extends BooleanConfigurationOption {

    public PayOnLowBalance() {
        super();
    }

    public PayOnLowBalance(Boolean value) {
        super(value);
    }

    @Override
    Config.ConfigPair<Boolean> getConfigPair() {
        return Config.getPayOnLowBalance();
    }

}
