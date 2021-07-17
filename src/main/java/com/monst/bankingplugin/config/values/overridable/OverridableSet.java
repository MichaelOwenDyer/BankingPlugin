package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.config.values.IConfigSet;

import java.util.List;
import java.util.Set;

abstract class OverridableSet<T> extends OverridableValue<List<String>, Set<T>> implements IConfigSet<T> {

    OverridableSet(String path, Set<T> defaultValue) {
        super(path, defaultValue);
    }

}
