package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.type.BooleanConfigurationValue;

public class EnableGriefPreventionIntegration extends BooleanConfigurationValue {

    public EnableGriefPreventionIntegration(BankingPlugin plugin) {
        super(plugin, "enable-griefprevention-integration", true);
    }

}
