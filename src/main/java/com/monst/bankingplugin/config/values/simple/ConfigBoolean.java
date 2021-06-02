package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.config.values.ConfigValue;
import com.monst.bankingplugin.config.values.IConfigBoolean;

public class ConfigBoolean extends ConfigValue<Boolean> implements IConfigBoolean {

    public ConfigBoolean(String path, Boolean defaultValue) {
        super(path, defaultValue);
    }

}
