package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.config.values.ConfigValue;
import com.monst.bankingplugin.config.values.IConfigDouble;
import com.monst.bankingplugin.utils.Utils;

public class ConfigDouble extends ConfigValue<Double> implements IConfigDouble {

    public ConfigDouble(String path, Double defaultValue) {
        super(path, defaultValue); // TODO: Apply constraint
    }

    @Override
    public String format(Double value) {
        return Utils.format(value);
    }

}
