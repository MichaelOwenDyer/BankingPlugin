package com.monst.bankingplugin.config.values;

public class EnableWorldGuardIntegration extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public EnableWorldGuardIntegration() {
        super("enable-worldguard-integration", true);
    }

}
