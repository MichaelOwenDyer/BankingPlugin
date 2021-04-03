package com.monst.bankingplugin.config.values.overridable;

import org.bukkit.configuration.file.FileConfiguration;

import javax.annotation.Nonnull;

public abstract class OverridableBoolean extends OverridableValue<Boolean> {

    OverridableBoolean(String path, Boolean defaultValue) {
        super(path, defaultValue, FileConfiguration::getBoolean);
    }

    @Override
    public OverriddenValue<Boolean> override(Boolean value) {
        return new OverriddenBoolean(this, value);
    }

    @Override
    public Boolean parse(@Nonnull String input) {
        return Boolean.parseBoolean(input);
    }

}
