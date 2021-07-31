package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class AllowOverride extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public AllowOverride(BankingPlugin plugin, String path) {
        super(plugin, path + ".allow-override", true);
    }

}
