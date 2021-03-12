package com.monst.bankingplugin.banking.bank.configuration;

import com.monst.bankingplugin.config.Config;

public class InitialInterestDelay extends IntegerConfigurationOption {

    public InitialInterestDelay() {
        super();
    }

    public InitialInterestDelay(Integer value) {
        super(value);
    }

    @Override
    Config.ConfigPair<Integer> getConfigPair() {
        return Config.getInitialInterestDelay();
    }

}
