package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.BooleanTransformer;

public class StickyDefaults extends ConfigurationValue<Boolean> {

    public StickyDefaults(BankingPlugin plugin) {
        super(plugin, "sticky-defaults", false, new BooleanTransformer());
    }

}
