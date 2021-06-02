package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.config.values.ConfigValue;
import com.monst.bankingplugin.config.values.IConfigInteger;

public class ConfigInteger extends ConfigValue<Integer> implements IConfigInteger {

    public ConfigInteger(String path, Integer defaultValue) {
        super(path, defaultValue);
    }

}
