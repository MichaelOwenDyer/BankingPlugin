package com.monst.bankingplugin.config.values;

public class TrustOnTransfer extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public TrustOnTransfer() {
        super("trust-on-transfer", true);
    }

}
