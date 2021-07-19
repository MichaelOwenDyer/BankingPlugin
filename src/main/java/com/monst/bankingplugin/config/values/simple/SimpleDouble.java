package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.config.values.ConfigValue;
import com.monst.bankingplugin.config.values.IConfigDouble;

public class SimpleDouble extends ConfigValue<Double, Double> implements IConfigDouble {

    public SimpleDouble(String path, Double defaultValue) {
        super(path, defaultValue);
    }

}
