package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.config.values.UnaryConfigValue;
import com.monst.bankingplugin.config.values.IConfigBoolean;

public class SimpleBoolean extends UnaryConfigValue<Boolean> implements IConfigBoolean {

    public SimpleBoolean(String path, Boolean defaultValue) {
        super(path, defaultValue);
    }

}
