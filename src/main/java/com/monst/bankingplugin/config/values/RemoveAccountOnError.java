package com.monst.bankingplugin.config.values;

public class RemoveAccountOnError extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public RemoveAccountOnError() {
        super("remove-account-on-error", false);
    }

}
