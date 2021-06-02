package com.monst.bankingplugin.config.values.simple;

public class DefaultAccountLimit extends ConfigInteger {

    public DefaultAccountLimit() {
        super("default-limits.account", 2);
    }

}
