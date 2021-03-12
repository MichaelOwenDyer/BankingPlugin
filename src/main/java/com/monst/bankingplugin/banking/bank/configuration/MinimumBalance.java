package com.monst.bankingplugin.banking.bank.configuration;

import com.monst.bankingplugin.config.Config;

public class MinimumBalance extends DoubleConfigurationOption {

    public MinimumBalance() {
        super();
    }

    public MinimumBalance(Double value) {
        super(value);
    }

    @Override
    Config.ConfigPair<Double> getConfigPair() {
        return Config.getMinimumBalance();
    }

}
