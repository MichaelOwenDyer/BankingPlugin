package com.monst.bankingplugin.config.values;

public class DefaultBankLimit extends ConfigValue<Integer, Integer> implements NativeInteger {

    public DefaultBankLimit() {
        super("default-limits.bank", 1);
    }

}
