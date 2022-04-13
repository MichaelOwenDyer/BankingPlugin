package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;

public class ConfirmOnTransfer extends BooleanConfigurationValue {

    public ConfirmOnTransfer(BankingPlugin plugin) {
        super(plugin, "confirm-on-transfer", true);
    }

}
