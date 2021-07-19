package com.monst.bankingplugin.config.values;

class SimpleBoolean extends ConfigValue<Boolean, Boolean> implements IConfigBoolean {

    public SimpleBoolean(String path, Boolean defaultValue) {
        super(path, defaultValue);
    }

}
