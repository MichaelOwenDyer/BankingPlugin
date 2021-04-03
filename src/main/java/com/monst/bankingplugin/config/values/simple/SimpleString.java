package com.monst.bankingplugin.config.values.simple;

import org.bukkit.configuration.file.FileConfiguration;

public abstract class SimpleString extends SimpleValue<String> {

    public SimpleString(String path, String defaultValue) {
        super(path, defaultValue, FileConfiguration::getString);
    }

    @Override
    public String parse(String input) {
        return input;
    }

}
