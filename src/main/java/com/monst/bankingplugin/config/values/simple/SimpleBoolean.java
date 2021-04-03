package com.monst.bankingplugin.config.values.simple;

import org.bukkit.configuration.file.FileConfiguration;

public abstract class SimpleBoolean extends SimpleValue<Boolean> {

    public SimpleBoolean(String path, Boolean defaultValue) {
        super(path, defaultValue, FileConfiguration::getBoolean);
    }

    @Override
    public Boolean parse(String input) {
        return Boolean.parseBoolean(input);
    }

}
