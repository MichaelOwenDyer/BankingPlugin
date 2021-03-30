package com.monst.bankingplugin.config.values.simple;

import org.bukkit.configuration.file.FileConfiguration;

public class SimpleString extends SimpleConfigValue<String> {

    public SimpleString(String path, String defaultValue) {
        super(path, defaultValue, FileConfiguration::getString);
    }

    @Override
    String parse(String input) {
        return input;
    }

}
