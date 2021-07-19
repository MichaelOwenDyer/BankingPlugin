package com.monst.bankingplugin.config.values;

public class ConfirmOnRemove extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public ConfirmOnRemove() {
        super("confirm-on-remove", true);
    }

}
