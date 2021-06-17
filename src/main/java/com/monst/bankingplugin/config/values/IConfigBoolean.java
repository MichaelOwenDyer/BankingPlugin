package com.monst.bankingplugin.config.values;

import org.bukkit.configuration.MemoryConfiguration;

public interface IConfigBoolean extends IConfigValue<Boolean> {

    @Override
    default Boolean parse(String input) {
        return Boolean.parseBoolean(input);
    }

    @Override
    default Boolean readFromFile(MemoryConfiguration config, String path) {
        return config.getBoolean(path);
    }

}
