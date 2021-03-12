package com.monst.bankingplugin.banking.bank.configuration;

import com.monst.bankingplugin.config.Config;

public class PlayerBankAccountLimit extends IntegerConfigurationOption {

    public PlayerBankAccountLimit() {
        super();
    }

    public PlayerBankAccountLimit(Integer value) {
        super(value);
    }

    @Override
    Config.ConfigPair<Integer> getConfigPair() {
        return Config.getPlayerBankAccountLimit();
    }

}
