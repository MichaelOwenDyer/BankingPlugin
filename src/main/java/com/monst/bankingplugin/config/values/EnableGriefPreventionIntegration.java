package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class EnableGriefPreventionIntegration extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public EnableGriefPreventionIntegration(BankingPlugin plugin) {
        super(plugin, "enable-griefprevention-integration", true);
    }

}
