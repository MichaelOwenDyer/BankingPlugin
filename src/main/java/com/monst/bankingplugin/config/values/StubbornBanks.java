package com.monst.bankingplugin.config.values;

public class StubbornBanks extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public StubbornBanks() {
        super("stubborn-banks", false);
    }

}
