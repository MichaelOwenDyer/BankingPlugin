package com.monst.bankingplugin.config.values;

abstract class OverridableBoolean extends OverridableValue<Boolean, Boolean> implements IConfigBoolean {

    OverridableBoolean(String path, Boolean defaultValue) {
        super(path, defaultValue);
    }

}
