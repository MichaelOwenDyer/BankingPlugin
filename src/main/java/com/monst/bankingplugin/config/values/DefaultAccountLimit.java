package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class DefaultAccountLimit extends ConfigValue<Integer, Integer> implements NativeInteger {

    public DefaultAccountLimit(BankingPlugin plugin) {
        super(plugin, "default-limits.account", 2);
    }

}
