package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.BooleanTransformer;

public class EnableStartupMessage extends ConfigurationValue<Boolean> {

    public EnableStartupMessage(BankingPlugin plugin) {
        super(plugin, "enable-startup-message", true, new BooleanTransformer());
    }

}
