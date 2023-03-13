package com.monst.bankingplugin.configuration.transform;

import org.codemc.worldguardwrapper.flag.WrappedState;

public class WrappedStateTransformer implements Transformer<WrappedState> {
    
    @Override
    public WrappedState parse(String s) {
        if (s.equalsIgnoreCase("allow") || s.equalsIgnoreCase("true"))
            return WrappedState.ALLOW;
        return WrappedState.DENY;
    }
    
    @Override
    public Object toYaml(WrappedState wrappedState) {
        return wrappedState.name().toLowerCase();
    }
    
}
