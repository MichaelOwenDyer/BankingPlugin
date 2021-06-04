package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.config.values.ConfigValue;
import org.bukkit.configuration.MemoryConfiguration;

public class SimpleString extends ConfigValue<String> {

    public SimpleString(String path, String defaultValue) {
        super(path, defaultValue);
    }

    @Override
    public String readValueFromFile(MemoryConfiguration config, String path) {
        return config.getString(path);
    }

    @Override
    public String parse(String input) {
        return input;
    }

}
