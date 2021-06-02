package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.config.values.simple.ConfigBoolean;

public class WorldGuardDefaultFlagValue extends ConfigBoolean {

    public WorldGuardDefaultFlagValue() {
        super("worldguard-default-flag-value", false);
    }

}
