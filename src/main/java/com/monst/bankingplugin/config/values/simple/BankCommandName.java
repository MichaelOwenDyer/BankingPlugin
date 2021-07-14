package com.monst.bankingplugin.config.values.simple;

public class BankCommandName extends SimpleString {

    public BankCommandName() {
        super("command-name-bank", "bank");
    }

    @Override
    protected boolean isHotSwappable() {
        return false;
    }

}
