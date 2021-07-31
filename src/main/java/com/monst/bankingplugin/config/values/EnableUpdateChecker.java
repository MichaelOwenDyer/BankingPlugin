package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class EnableUpdateChecker extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public EnableUpdateChecker(BankingPlugin plugin) {
        super(plugin, "enable-update-checker", true);
    }

}
