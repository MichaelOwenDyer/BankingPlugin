package com.monst.bankingplugin.config.values.overridable;

import org.bukkit.configuration.file.FileConfiguration;

public abstract class OverridableBoolean extends OverridableValue<Boolean> {

    OverridableBoolean(String path, Boolean defaultValue) {
        super(path, defaultValue, FileConfiguration::getBoolean);
    }

    @Override
    public OverriddenValue<Boolean> override(Boolean value) {
        return new OverriddenBoolean(this, value);
    }

}
