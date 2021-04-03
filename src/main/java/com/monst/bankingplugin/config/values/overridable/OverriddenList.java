package com.monst.bankingplugin.config.values.overridable;

import java.util.List;

class OverriddenList<T> extends OverriddenValue<List<T>> {

    OverriddenList(OverridableList<T> attribute, List<T> value) {
        super(attribute, value);
    }

}
