package com.monst.bankingplugin.config.values;

public class ControlCommandName extends ConfigValue<String, String> implements NativeString {

    public ControlCommandName() {
        super("command-name-control", "bp");
    }

    @Override
    public boolean isHotSwappable() {
        return false;
    }

}
