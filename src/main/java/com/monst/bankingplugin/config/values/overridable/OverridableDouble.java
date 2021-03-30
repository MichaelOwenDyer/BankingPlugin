package com.monst.bankingplugin.config.values.overridable;

import org.bukkit.configuration.file.FileConfiguration;

public abstract class OverridableDouble extends OverridableValue<Double> {

    OverridableDouble(String path, Double defaultValue) {
        super(path, defaultValue, FileConfiguration::getDouble);
    }

    @Override
    public OverriddenValue<Double> override(Double value) {
        return new OverriddenDouble(this, value);
    }

}
