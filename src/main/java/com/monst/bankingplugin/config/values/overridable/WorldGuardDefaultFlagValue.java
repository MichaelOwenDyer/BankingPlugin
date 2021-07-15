package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.config.values.simple.SimpleBoolean;
import org.codemc.worldguardwrapper.flag.WrappedState;

public class WorldGuardDefaultFlagValue extends SimpleBoolean {

    public WorldGuardDefaultFlagValue() {
        super("worldguard-default-flag-value", false);
    }

    @Override
    public boolean isHotSwappable() {
        return false;
    }

    public WrappedState getWrappedState() {
        return get() ? WrappedState.ALLOW : WrappedState.DENY;
    }

}
