package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;

public class EnableStartupUpdateCheck extends BooleanConfigurationValue {

    public EnableStartupUpdateCheck(BankingPlugin plugin) {
        super(plugin, "enable-startup-update-check", true);
    }

}
