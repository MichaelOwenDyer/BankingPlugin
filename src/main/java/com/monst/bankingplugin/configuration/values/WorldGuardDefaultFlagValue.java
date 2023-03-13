package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.WrappedStateTransformer;
import org.codemc.worldguardwrapper.flag.WrappedState;

public class WorldGuardDefaultFlagValue extends ConfigurationValue<WrappedState> {

    public WorldGuardDefaultFlagValue(BankingPlugin plugin) {
        super(plugin, "worldguard-default-flag-value", WrappedState.DENY, new WrappedStateTransformer());
    }

    @Override
    public boolean isHotSwappable() {
        return false;
    }

}
