package com.monst.bankingplugin.config.values;

class SimpleDouble extends ConfigValue<Double, Double> implements IConfigDouble {

    public SimpleDouble(String path, Double defaultValue) {
        super(path, defaultValue);
    }

}
