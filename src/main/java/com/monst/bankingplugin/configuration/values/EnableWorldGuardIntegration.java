package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.type.BooleanConfigurationValue;

public class EnableWorldGuardIntegration extends BooleanConfigurationValue {

    public EnableWorldGuardIntegration(BankingPlugin plugin) {
        super(plugin, "enable-worldguard-integration", true);
    }

}
