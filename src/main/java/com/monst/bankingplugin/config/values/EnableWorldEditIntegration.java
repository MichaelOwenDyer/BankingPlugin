package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class EnableWorldEditIntegration extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public EnableWorldEditIntegration(BankingPlugin plugin) {
        super(plugin, "enable-worldedit-integration", true);
    }

}
