package com.monst.bankingplugin.banking.bank.configuration;

import com.monst.bankingplugin.config.Config;

public class LowBalanceFee extends DoubleConfigurationOption {

    public LowBalanceFee() {
        super();
    }

    public LowBalanceFee(Double value) {
        super(value);
    }

    @Override
    Config.ConfigPair<Double> getConfigPair() {
        return Config.getLowBalanceFee();
    }

}
