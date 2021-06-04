package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.config.values.ConfigValue;
import com.monst.bankingplugin.config.values.IConfigBoolean;

public class SimpleBoolean extends ConfigValue<Boolean> implements IConfigBoolean {

    public SimpleBoolean(String path, Boolean defaultValue) {
        super(path, defaultValue);
    }

}
