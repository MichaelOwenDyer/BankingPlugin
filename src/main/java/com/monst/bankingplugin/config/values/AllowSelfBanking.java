package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class AllowSelfBanking extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public AllowSelfBanking(BankingPlugin plugin) {
        super(plugin, "allow-self-banking", false);
    }

}
