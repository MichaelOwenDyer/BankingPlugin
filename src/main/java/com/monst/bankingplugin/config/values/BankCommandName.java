package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class BankCommandName extends ConfigValue<String, String> implements NativeString {

    public BankCommandName(BankingPlugin plugin) {
        super(plugin, "command-name-bank", "bank");
    }

    @Override
    public boolean isHotSwappable() {
        return false;
    }

}
