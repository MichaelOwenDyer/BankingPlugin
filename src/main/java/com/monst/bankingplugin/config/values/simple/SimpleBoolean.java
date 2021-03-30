package com.monst.bankingplugin.config.values.simple;

import org.bukkit.configuration.file.FileConfiguration;

public class SimpleBoolean extends SimpleConfigValue<Boolean> {

    public SimpleBoolean(String path, Boolean defaultValue) {
        super(path, defaultValue, FileConfiguration::getBoolean);
    }

    @Override
    Boolean parse(String input) {
        return Boolean.parseBoolean(input);
    }

}
