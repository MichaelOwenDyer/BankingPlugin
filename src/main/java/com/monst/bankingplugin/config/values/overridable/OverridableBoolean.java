package com.monst.bankingplugin.config.values.overridable;

import org.bukkit.configuration.MemoryConfiguration;

import javax.annotation.Nonnull;

public abstract class OverridableBoolean extends OverridableValue<Boolean> {

    OverridableBoolean(String path, Boolean defaultValue) {
        super(path, defaultValue, MemoryConfiguration::getBoolean);
    }

    @Override
    public Boolean parse(@Nonnull String input) {
        return Boolean.parseBoolean(input);
    }

}
