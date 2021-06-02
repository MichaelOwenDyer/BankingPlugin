package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.config.values.overridable.OverriddenValue;
import com.monst.bankingplugin.config.values.simple.AllowOverride;

public interface Overridable<T> {

    AllowOverride getAllowOverride();

    OverriddenValue<T> override(T value);

    default boolean isOverridable() {
        return getAllowOverride().get();
    }

}
