package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.type.BooleanConfigurationValue;

public class EnableDebugLog extends BooleanConfigurationValue {

    public EnableDebugLog(BankingPlugin plugin) {
        super(plugin, "enable-debug-log", false);
    }

    @Override
    protected void beforeSet() {
        if (!get())
            plugin.debug("Debug log disabled.");
    }

    @Override
    protected void afterSet() {
        if (get())
            plugin.debug("Debug log enabled.");
    }

}
