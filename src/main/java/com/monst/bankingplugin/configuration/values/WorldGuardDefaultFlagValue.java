package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.type.ConfigurationValue;
import org.codemc.worldguardwrapper.flag.WrappedState;

public class WorldGuardDefaultFlagValue extends ConfigurationValue<WrappedState> {

    public WorldGuardDefaultFlagValue(BankingPlugin plugin) {
        super(plugin, "worldguard-default-flag-value", WrappedState.DENY);
    }

    @Override
    protected WrappedState parse(String s) {
        if (s.equalsIgnoreCase("allow"))
            return WrappedState.ALLOW;
        if (s.equalsIgnoreCase("deny"))
            return WrappedState.DENY;
        return Boolean.parseBoolean(s) ? WrappedState.ALLOW : WrappedState.DENY;
    }

    @Override
    protected Object convertToYamlType(WrappedState wrappedState) {
        return wrappedState.name().toLowerCase();
    }

    @Override
    public boolean isHotSwappable() {
        return false;
    }

}
