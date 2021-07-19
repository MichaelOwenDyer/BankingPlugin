package com.monst.bankingplugin.config.values;

public class EnableWorldEditIntegration extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public EnableWorldEditIntegration() {
        super("enable-worldedit-integration", true);
    }

}
