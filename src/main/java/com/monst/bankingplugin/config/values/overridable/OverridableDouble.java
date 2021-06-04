package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.config.values.IConfigDouble;

public abstract class OverridableDouble extends OverridableValue<Double> implements IConfigDouble {

    OverridableDouble(String path, Double defaultValue) {
        super(path, defaultValue);
    }

}
