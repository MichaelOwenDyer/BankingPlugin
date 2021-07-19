package com.monst.bankingplugin.config.values;

public class BankCommandName extends ConfigValue<String, String> implements NativeString {

    public BankCommandName() {
        super("command-name-bank", "bank");
    }

    @Override
    public boolean isHotSwappable() {
        return false;
    }

}
