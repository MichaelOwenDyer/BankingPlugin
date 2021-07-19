package com.monst.bankingplugin.config.values;

public class ConfirmOnTransfer extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public ConfirmOnTransfer() {
        super("confirm-on-transfer", true);
    }

}
