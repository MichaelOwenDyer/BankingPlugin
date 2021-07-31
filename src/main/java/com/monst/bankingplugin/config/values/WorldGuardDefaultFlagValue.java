package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;
import org.codemc.worldguardwrapper.flag.WrappedState;

public class WorldGuardDefaultFlagValue extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public WorldGuardDefaultFlagValue(BankingPlugin plugin) {
        super(plugin, "worldguard-default-flag-value", false);
    }

    @Override
    public boolean isHotSwappable() {
        return false;
    }

    public WrappedState getWrappedState() {
        return get() ? WrappedState.ALLOW : WrappedState.DENY;
    }

}
