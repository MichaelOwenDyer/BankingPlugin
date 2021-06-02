package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.config.values.IConfigSet;

import java.util.Set;

abstract class OverridableSet<T> extends OverridableValue<Set<T>> implements IConfigSet<T> {

    OverridableSet(String path, Set<T> prescribedValue) {
        super(path, prescribedValue);
    }

}
