package com.monst.bankingplugin.config.values;

public class EnableGriefPreventionIntegration extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public EnableGriefPreventionIntegration() {
        super("enable-griefprevention-integration", true);
    }

}
