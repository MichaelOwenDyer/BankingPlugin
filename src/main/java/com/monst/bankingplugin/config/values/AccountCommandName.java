package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;

public class AccountCommandName extends ConfigValue<String, String> implements NativeString {

    public AccountCommandName(BankingPlugin plugin) {
        super(plugin, "command-name-account", "account");
    }

    @Override
    public boolean isHotSwappable() {
        return false;
    }

}
