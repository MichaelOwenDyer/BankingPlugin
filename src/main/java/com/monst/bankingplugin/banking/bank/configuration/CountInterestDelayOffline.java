package com.monst.bankingplugin.banking.bank.configuration;

import com.monst.bankingplugin.config.Config;

public class CountInterestDelayOffline extends BooleanConfigurationOption {

    public CountInterestDelayOffline() {
        super();
    }

    public CountInterestDelayOffline(Boolean value) {
        super(value);
    }

    @Override
    protected Config.ConfigPair<Boolean> getConfigPair() {
        return Config.getCountInterestDelayOffline();
    }

}
