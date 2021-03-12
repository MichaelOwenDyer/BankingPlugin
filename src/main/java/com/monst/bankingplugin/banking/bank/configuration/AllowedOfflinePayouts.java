package com.monst.bankingplugin.banking.bank.configuration;

import com.monst.bankingplugin.config.Config;

public class AllowedOfflinePayouts extends IntegerConfigurationOption {

    public AllowedOfflinePayouts() {
        super();
    }

    public AllowedOfflinePayouts(Integer value) {
        super(value);
    }

    @Override
    protected Config.ConfigPair<Integer> getConfigPair() {
        return Config.getAllowedOfflinePayouts();
    }

}
