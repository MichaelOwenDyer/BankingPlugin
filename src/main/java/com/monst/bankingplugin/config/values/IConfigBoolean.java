package com.monst.bankingplugin.config.values;

import org.bukkit.configuration.MemoryConfiguration;

public interface IConfigBoolean extends IConfigValue<Boolean> {

    @Override
    default Boolean readValueFromFile(MemoryConfiguration config, String path) {
        if (isPathMissing())
            return null;
        return config.getBoolean(path);
    }

    @Override
    default Boolean parse(String input) {
        return Boolean.parseBoolean(input);
    }

}
