package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.BooleanTransformer;

/**
 * Configuration value for whether to confirm on removal of a bank or account.
 */
public class ConfirmOnRemove extends ConfigurationValue<Boolean> {

    public ConfirmOnRemove(BankingPlugin plugin) {
        super(plugin, "confirm-on-remove", true, new BooleanTransformer());
    }

}
