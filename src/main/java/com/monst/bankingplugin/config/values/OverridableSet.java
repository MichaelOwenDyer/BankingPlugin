package com.monst.bankingplugin.config.values;

import java.util.List;
import java.util.Set;

abstract class OverridableSet<T> extends OverridableValue<List<String>, Set<T>> implements IConfigSet<T> {

    OverridableSet(String path, Set<T> defaultValue) {
        super(path, defaultValue);
    }

}
