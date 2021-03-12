package com.monst.bankingplugin.banking.bank.configuration;

import com.monst.bankingplugin.config.Config;

public class WithdrawalMultiplierDecrement extends IntegerConfigurationOption {

    public WithdrawalMultiplierDecrement() {
        super();
    }

    public WithdrawalMultiplierDecrement(Integer value) {
        super(value);
    }

    @Override
    Config.ConfigPair<Integer> getConfigPair() {
        return Config.getWithdrawalMultiplierDecrement();
    }

}
