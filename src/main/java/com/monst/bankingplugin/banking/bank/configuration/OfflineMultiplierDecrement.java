package com.monst.bankingplugin.banking.bank.configuration;

import com.monst.bankingplugin.config.Config;

public class OfflineMultiplierDecrement extends IntegerConfigurationOption {

    public OfflineMultiplierDecrement() {
        super();
    }

    public OfflineMultiplierDecrement(Integer value) {
        super(value);
    }

    @Override
    Config.ConfigPair<Integer> getConfigPair() {
        return Config.getOfflineMultiplierDecrement();
    }

}
