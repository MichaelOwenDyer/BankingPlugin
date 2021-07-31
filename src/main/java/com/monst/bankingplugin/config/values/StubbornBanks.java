package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class StubbornBanks extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public StubbornBanks(BankingPlugin plugin) {
        super(plugin, "stubborn-banks", false);
    }

}
