package com.monst.bankingplugin.config.values.simple;

public class AllowOverride extends ConfigBoolean {

    public AllowOverride(String path) {
        super(path + ".allow-override", true);
    }

}
