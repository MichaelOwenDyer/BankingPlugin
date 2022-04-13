package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;

public class StickyDefaults extends BooleanConfigurationValue {

    public StickyDefaults(BankingPlugin plugin) {
        super(plugin, "sticky-defaults", false);
    }

}
