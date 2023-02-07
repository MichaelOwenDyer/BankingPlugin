package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.type.BooleanConfigurationValue;

public class ConfirmOnRemoveAll extends BooleanConfigurationValue {

    public ConfirmOnRemoveAll(BankingPlugin plugin) {
        super(plugin, "confirm-on-remove-all", true);
    }

}
