package com.monst.bankingplugin.banking.bank.configuration;

import com.monst.bankingplugin.config.Config;

public class AllowedOfflinePayoutsBeforeReset extends IntegerConfigurationOption {

    public AllowedOfflinePayoutsBeforeReset() {
        super();
    }

    public AllowedOfflinePayoutsBeforeReset(Integer value) {
        super(value);
    }

    @Override
    Config.ConfigPair<Integer> getConfigPair() {
        return Config.getAllowedOfflinePayoutsBeforeReset();
    }

}
