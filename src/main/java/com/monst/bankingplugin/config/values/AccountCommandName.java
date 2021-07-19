package com.monst.bankingplugin.config.values;

public class AccountCommandName extends SimpleString {

    public AccountCommandName() {
        super("command-name-account", "account");
    }

    @Override
    public boolean isHotSwappable() {
        return false;
    }

}
