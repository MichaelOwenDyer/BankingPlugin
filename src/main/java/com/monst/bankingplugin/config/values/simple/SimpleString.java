package com.monst.bankingplugin.config.values.simple;

import org.bukkit.configuration.MemoryConfiguration;

public abstract class SimpleString extends SimpleValue<String> {

    public SimpleString(String path, String defaultValue) {
        super(path, defaultValue, MemoryConfiguration::getString);
    }

    @Override
    public String parse(String input) {
        return input;
    }

}
