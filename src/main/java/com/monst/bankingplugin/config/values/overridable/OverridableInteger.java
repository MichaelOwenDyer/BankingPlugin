package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.config.values.IConfigInteger;

abstract class OverridableInteger extends OverridableValue<Integer> implements IConfigInteger {

    OverridableInteger(String path, Integer defaultValue) {
        super(path, defaultValue);
    }

}
