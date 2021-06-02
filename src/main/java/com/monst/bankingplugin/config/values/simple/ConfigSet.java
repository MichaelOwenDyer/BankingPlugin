package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.config.values.ConfigValue;
import com.monst.bankingplugin.config.values.IConfigSet;

import java.util.Set;

public abstract class ConfigSet<T> extends ConfigValue<Set<T>> implements IConfigSet<T> {

    public ConfigSet(String path, Set<T> defaultValue) {
        super(path, defaultValue);
    }

}
