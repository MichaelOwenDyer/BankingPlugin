package com.monst.bankingplugin.config.values;

public class DefaultAccountLimit extends ConfigValue<Integer, Integer> implements NativeInteger {

    public DefaultAccountLimit() {
        super("default-limits.account", 2);
    }

}
