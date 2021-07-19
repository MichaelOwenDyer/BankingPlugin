package com.monst.bankingplugin.config.values;

public class EnableUpdateChecker extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public EnableUpdateChecker() {
        super("enable-update-checker", true);
    }

}
