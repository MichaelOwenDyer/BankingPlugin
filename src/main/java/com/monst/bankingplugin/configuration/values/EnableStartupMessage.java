package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;

public class EnableStartupMessage extends BooleanConfigurationValue {

    public EnableStartupMessage(BankingPlugin plugin) {
        super(plugin, "enable-startup-message", true);
    }

}