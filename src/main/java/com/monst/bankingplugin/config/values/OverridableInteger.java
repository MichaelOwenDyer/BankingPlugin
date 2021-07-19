package com.monst.bankingplugin.config.values;

abstract class OverridableInteger extends OverridableValue<Integer, Integer> implements IConfigInteger {

    OverridableInteger(String path, Integer defaultValue) {
        super(path, defaultValue);
    }

}
