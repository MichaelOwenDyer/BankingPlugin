package com.monst.bankingplugin.config.values;

class SimpleString extends ConfigValue<String, String> implements IConfigString {

    public SimpleString(String path, String defaultValue) {
        super(path, defaultValue);
    }

}
