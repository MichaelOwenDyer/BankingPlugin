package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class ControlCommandName extends ConfigValue<String, String> implements NativeString {

    public ControlCommandName(BankingPlugin plugin) {
        super(plugin, "command-name-control", "bp");
    }

    @Override
    public boolean isHotSwappable() {
        return false;
    }

}
