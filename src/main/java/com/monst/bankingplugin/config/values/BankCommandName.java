package com.monst.bankingplugin.config.values;

public class BankCommandName extends SimpleString {

    public BankCommandName() {
        super("command-name-bank", "bank");
    }

    @Override
    public boolean isHotSwappable() {
        return false;
    }

}