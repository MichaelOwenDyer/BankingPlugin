package com.monst.bankingplugin.config.values;

abstract class OverridableDouble extends OverridableValue<Double, Double> implements IConfigDouble {

    OverridableDouble(String path, Double defaultValue) {
        super(path, defaultValue);
    }

}
