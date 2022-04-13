package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;

/**
 * Configuration value for whether to confirm on removal of a bank or account.
 */
public class ConfirmOnRemove extends BooleanConfigurationValue {

    public ConfirmOnRemove(BankingPlugin plugin) {
        super(plugin, "confirm-on-remove", true);
    }

}
