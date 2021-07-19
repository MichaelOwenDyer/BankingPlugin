package com.monst.bankingplugin.config.values;

import java.util.List;

abstract class OverridableList<T> extends OverridableValue<List<String>, List<T>> implements IConfigList<T> {

    OverridableList(String path, List<T> defaultValue) {
        super(path, defaultValue);
    }

}
