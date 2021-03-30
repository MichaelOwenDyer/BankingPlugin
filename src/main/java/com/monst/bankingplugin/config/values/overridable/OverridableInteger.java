package com.monst.bankingplugin.config.values.overridable;

import org.bukkit.configuration.file.FileConfiguration;

abstract class OverridableInteger extends OverridableValue<Integer> {

    OverridableInteger(String path, Integer defaultValue) {
        super(path, defaultValue, FileConfiguration::getInt);
    }

    @Override
    public OverriddenValue<Integer> override(Integer value) {
        return new OverriddenInteger(this, value);
    }

}
