package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.config.values.UnaryConfigValue;
import com.monst.bankingplugin.config.values.IConfigInteger;

public class SimpleInteger extends UnaryConfigValue<Integer> implements IConfigInteger {

    public SimpleInteger(String path, Integer defaultValue) {
        super(path, defaultValue);
    }

}
