package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.BooleanTransformer;

public class ConfirmOnRemoveAll extends ConfigurationValue<Boolean> {

    public ConfirmOnRemoveAll(BankingPlugin plugin) {
        super(plugin, "confirm-on-remove-all", true, new BooleanTransformer());
    }

}
