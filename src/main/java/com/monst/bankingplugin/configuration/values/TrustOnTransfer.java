package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;

public class TrustOnTransfer extends BooleanConfigurationValue {

    public TrustOnTransfer(BankingPlugin plugin) {
        super(plugin, "trust-on-transfer", true);
    }

}
