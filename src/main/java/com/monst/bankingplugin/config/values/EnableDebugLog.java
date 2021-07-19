package com.monst.bankingplugin.config.values;

public class EnableDebugLog extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public EnableDebugLog() {
        super("enable-debug-log", false);
    }

    @Override
    protected void beforeSet(Boolean newValue) {
        if (!newValue)
            PLUGIN.debug("Debug log disabled.");
    }

    @Override
    protected void afterSet(Boolean newValue) {
        if (newValue)
            PLUGIN.debug("Debug log enabled.");
    }

}
