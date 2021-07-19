package com.monst.bankingplugin.config.values;

public class EnableStartupMessage extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public EnableStartupMessage() {
        super("enable-startup-message", true);
    }

}
