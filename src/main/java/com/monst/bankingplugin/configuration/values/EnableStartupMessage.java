package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.type.BooleanConfigurationValue;

public class EnableStartupMessage extends BooleanConfigurationValue {

    public EnableStartupMessage(BankingPlugin plugin) {
        super(plugin, "enable-startup-message", true);
    }

}
