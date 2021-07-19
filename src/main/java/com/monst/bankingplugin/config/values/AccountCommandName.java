package com.monst.bankingplugin.config.values;

public class AccountCommandName extends ConfigValue<String, String> implements NativeString {

    public AccountCommandName() {
        super("command-name-account", "account");
    }

    @Override
    public boolean isHotSwappable() {
        return false;
    }

}
