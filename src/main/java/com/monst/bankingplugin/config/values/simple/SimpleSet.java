package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.config.values.ConfigValue;
import com.monst.bankingplugin.config.values.IConfigSet;

import java.util.List;
import java.util.Set;

public abstract class SimpleSet<T> extends ConfigValue<List<String>, Set<T>> implements IConfigSet<T> {

    public SimpleSet(String path, Set<T> defaultValue) {
        super(path, defaultValue);
    }

}
