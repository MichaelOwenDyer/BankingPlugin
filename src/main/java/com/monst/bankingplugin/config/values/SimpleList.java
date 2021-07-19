package com.monst.bankingplugin.config.values;

import java.util.List;

abstract class SimpleList<T> extends ConfigValue<List<String>, List<T>> implements IConfigList<T> {

    public SimpleList(String path, List<T> defaultValue) {
        super(path, defaultValue);
    }

}
