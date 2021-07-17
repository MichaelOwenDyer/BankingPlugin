package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.config.values.UnaryConfigValue;
import com.monst.bankingplugin.config.values.IConfigString;

public class SimpleString extends UnaryConfigValue<String> implements IConfigString {

    public SimpleString(String path, String defaultValue) {
        super(path, defaultValue);
    }

}
