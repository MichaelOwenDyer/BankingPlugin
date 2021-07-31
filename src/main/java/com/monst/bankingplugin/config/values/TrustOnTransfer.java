package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class TrustOnTransfer extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public TrustOnTransfer(BankingPlugin plugin) {
        super(plugin, "trust-on-transfer", true);
    }

}
