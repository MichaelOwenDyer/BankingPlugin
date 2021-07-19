package com.monst.bankingplugin.config.values;

class SimpleInteger extends ConfigValue<Integer, Integer> implements IConfigInteger {

    public SimpleInteger(String path, Integer defaultValue) {
        super(path, defaultValue);
    }

}
