package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.type.BooleanConfigurationValue;

/**
 * Configuration value for whether to confirm on removal of a bank or account.
 */
public class ConfirmOnRemove extends BooleanConfigurationValue {

    public ConfirmOnRemove(BankingPlugin plugin) {
        super(plugin, "confirm-on-remove", true);
    }

}
