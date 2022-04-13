package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;

public class ConfirmOnRemoveAll extends BooleanConfigurationValue {

    public ConfirmOnRemoveAll(BankingPlugin plugin) {
        super(plugin, "confirm-on-remove-all", true);
    }

}