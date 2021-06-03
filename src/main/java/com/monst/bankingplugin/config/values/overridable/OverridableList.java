package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.config.values.IConfigList;

import java.util.List;

abstract class OverridableList<T> extends OverridableValue<List<T>> implements IConfigList<T> {

    OverridableList(String path, List<T> defaultValue) {
        super(path, defaultValue);
    }

}
