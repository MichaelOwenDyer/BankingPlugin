package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.type.BooleanConfigurationValue;

public class EnableDebugLog extends BooleanConfigurationValue {

    public EnableDebugLog(BankingPlugin plugin) {
        super(plugin, "enable-debug-log", false);
        plugin.setDebugLogEnabled(get());
    }

    @Override
    protected void afterSet() {
        plugin.setDebugLogEnabled(get());
    }

}
