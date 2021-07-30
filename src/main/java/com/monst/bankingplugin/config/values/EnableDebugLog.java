package com.monst.bankingplugin.config.values;

public class EnableDebugLog extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public EnableDebugLog() {
        super("enable-debug-log", false);
    }

    @Override
    void beforeSet() {
        if (!get())
            PLUGIN.debug("Debug log disabled.");
    }

    @Override
    void afterSet() {
        if (get())
            PLUGIN.debug("Debug log enabled.");
    }

}
