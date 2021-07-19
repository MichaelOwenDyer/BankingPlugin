package com.monst.bankingplugin.config.values;

public class ControlCommandName extends SimpleString {

    public ControlCommandName() {
        super("command-name-control", "bp");
    }

    @Override
    public boolean isHotSwappable() {
        return false;
    }

}
