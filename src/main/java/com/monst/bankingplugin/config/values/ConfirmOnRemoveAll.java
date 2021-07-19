package com.monst.bankingplugin.config.values;

public class ConfirmOnRemoveAll extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public ConfirmOnRemoveAll() {
        super("confirm-on-remove-all", true);
    }

}
