package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.config.values.simple.SimpleBoolean;

public class WorldGuardDefaultFlagValue extends SimpleBoolean {

    public WorldGuardDefaultFlagValue() {
        super("worldguard-default-flag-value", false);
    }

    @Override
    public boolean isHotSwappable() {
        return false;
    }

}
