package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.type.BooleanConfigurationValue;

/**
 * Configuration value that specifies whether a bank policy can be overridden.
 */
public class AllowOverride extends BooleanConfigurationValue {

    public AllowOverride(BankingPlugin plugin, String path) {
        super(plugin, BankPolicy.allowOverridePath(path), true);
    }

}
