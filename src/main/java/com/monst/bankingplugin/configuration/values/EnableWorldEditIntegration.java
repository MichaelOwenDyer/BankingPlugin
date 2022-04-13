package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;

public class EnableWorldEditIntegration extends BooleanConfigurationValue {

    public EnableWorldEditIntegration(BankingPlugin plugin) {
        super(plugin, "enable-worldedit-integration", true);
    }

}
