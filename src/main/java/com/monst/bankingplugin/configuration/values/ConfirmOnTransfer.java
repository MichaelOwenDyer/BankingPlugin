package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.type.BooleanConfigurationValue;

public class ConfirmOnTransfer extends BooleanConfigurationValue {

    public ConfirmOnTransfer(BankingPlugin plugin) {
        super(plugin, "confirm-on-transfer", true);
    }

}
