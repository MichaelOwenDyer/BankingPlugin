package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.config.values.IConfigDouble;
import com.monst.bankingplugin.utils.Utils;

public abstract class OverridableDouble extends OverridableValue<Double> implements IConfigDouble {

    OverridableDouble(String path, Double defaultValue) {
        super(path, defaultValue);
    }

    @Override
    public String format(Double value) {
        return Utils.format(value);
    }

}
