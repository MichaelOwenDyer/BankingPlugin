package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class EnableStartupMessage extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public EnableStartupMessage(BankingPlugin plugin) {
        super(plugin, "enable-startup-message", true);
    }

}
