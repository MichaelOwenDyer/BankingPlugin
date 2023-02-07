package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.type.BooleanConfigurationValue;

public class StickyDefaults extends BooleanConfigurationValue {

    public StickyDefaults(BankingPlugin plugin) {
        super(plugin, "sticky-defaults", false);
    }

}
