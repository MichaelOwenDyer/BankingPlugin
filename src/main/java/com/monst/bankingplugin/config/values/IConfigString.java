package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.DoubleParseException;
import org.bukkit.configuration.MemoryConfiguration;

public interface IConfigString extends IConfigValue<String> {

    @Override
    default String parse(String input) throws DoubleParseException {
        return input;
    }

    @Override
    default String readFromFile(MemoryConfiguration config, String path) {
        return config.getString(path);
    }

    @Override
    default String format(String value) {
        return value;
    }

}
