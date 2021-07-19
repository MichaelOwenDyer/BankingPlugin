package com.monst.bankingplugin.config.values;

import java.util.List;
import java.util.Set;

abstract class SimpleSet<T> extends ConfigValue<List<String>, Set<T>> implements IConfigSet<T> {

    public SimpleSet(String path, Set<T> defaultValue) {
        super(path, defaultValue);
    }

}
