package com.monst.bankingplugin.banking.bank.configuration;

import com.monst.bankingplugin.config.Config;

public class ReimburseAccountCreation extends BooleanConfigurationOption {

    public ReimburseAccountCreation() {
        super();
    }

    public ReimburseAccountCreation(Boolean value) {
        super(value);
    }

    @Override
    protected Config.ConfigPair<Boolean> getConfigPair() {
        return Config.getReimburseAccountCreation();
    }

}
