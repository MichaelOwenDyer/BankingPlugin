package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.config.values.UnaryConfigValue;
import com.monst.bankingplugin.config.values.IConfigDouble;

public class SimpleDouble extends UnaryConfigValue<Double> implements IConfigDouble {

    public SimpleDouble(String path, Double defaultValue) {
        super(path, defaultValue);
    }

}
