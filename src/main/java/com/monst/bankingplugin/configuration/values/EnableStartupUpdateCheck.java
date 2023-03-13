package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.BooleanTransformer;

public class EnableStartupUpdateCheck extends ConfigurationValue<Boolean> {

    public EnableStartupUpdateCheck(BankingPlugin plugin) {
        super(plugin, "enable-startup-update-check", true, new BooleanTransformer());
    }

}
