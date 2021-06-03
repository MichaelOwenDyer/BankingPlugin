package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.config.values.IConfigBoolean;

public class OverridableBoolean extends OverridableValue<Boolean> implements IConfigBoolean {

    OverridableBoolean(String path, Boolean defaultValue) {
        super(path, defaultValue);
    }

}
