package com.monst.bankingplugin.config.values;

public class AllowOverride extends ConfigValue<Boolean, Boolean> implements NativeBoolean {

    public AllowOverride(String path) {
        super(path + ".allow-override", true);
    }

}
