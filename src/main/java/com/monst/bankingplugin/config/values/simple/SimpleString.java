package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.config.values.ConfigValue;
import com.monst.bankingplugin.config.values.IConfigString;

public class SimpleString extends ConfigValue<String, String> implements IConfigString {

    public SimpleString(String path, String defaultValue) {
        super(path, defaultValue);
    }

}
