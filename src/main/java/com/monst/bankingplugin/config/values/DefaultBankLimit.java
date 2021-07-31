package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class DefaultBankLimit extends ConfigValue<Integer, Integer> implements NativeInteger {

    public DefaultBankLimit(BankingPlugin plugin) {
        super(plugin, "default-limits.bank", 1);
    }

}
