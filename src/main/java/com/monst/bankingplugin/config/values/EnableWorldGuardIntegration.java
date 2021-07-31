package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class EnableWorldGuardIntegration extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public EnableWorldGuardIntegration(BankingPlugin plugin) {
        super(plugin, "enable-worldguard-integration", true);
    }

}
