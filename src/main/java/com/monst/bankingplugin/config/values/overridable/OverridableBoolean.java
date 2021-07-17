package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.config.values.IConfigBoolean;

public abstract class OverridableBoolean extends OverridableValue<Boolean, Boolean> implements IConfigBoolean {

    OverridableBoolean(String path, Boolean defaultValue) {
        super(path, defaultValue);
    }

}
