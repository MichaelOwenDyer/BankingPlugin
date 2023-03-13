package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.BooleanTransformer;

public class EnableDebugLog extends ConfigurationValue<Boolean> {

    public EnableDebugLog(BankingPlugin plugin) {
        super(plugin, "enable-debug-log", false, new BooleanTransformer());
        plugin.setDebugLogEnabled(get());
    }

    @Override
    protected void afterSet() {
        plugin.setDebugLogEnabled(get());
    }

}
