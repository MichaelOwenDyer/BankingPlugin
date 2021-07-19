package com.monst.bankingplugin.config.values;

public class AllowOverride extends SimpleBoolean {

    public AllowOverride(String path) {
        super(path + ".allow-override", true);
    }

}
