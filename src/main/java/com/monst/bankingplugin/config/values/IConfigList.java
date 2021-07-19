package com.monst.bankingplugin.config.values;

import java.util.ArrayList;
import java.util.List;

interface IConfigList<T> extends IConfigCollection<T, List<T>> {

    @Override
    default List<T> getEmptyCollection() {
        return new ArrayList<>();
    }

}
