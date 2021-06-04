package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.DoubleParseException;
import org.bukkit.configuration.MemoryConfiguration;

public interface IConfigString extends IConfigValue<String> {

    @Override
    default String readValueFromFile(MemoryConfiguration config, String path) {
        if (isPathMissing())
            return null;
        return config.getString(path);
    }

    @Override
    default String parse(String input) throws DoubleParseException {
        return input;
    }

    @Override
    default String format(String value) {
        return value;
    }

}
