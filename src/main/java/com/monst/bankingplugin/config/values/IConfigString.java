package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.CorruptedValueException;
import org.bukkit.configuration.MemoryConfiguration;

public interface IConfigString extends IConfigValue<String> {

    @Override
    default String parse(String input) {
        return input;
    }

    @Override
    default String readFromFile(MemoryConfiguration config, String path) throws CorruptedValueException {
        if (!config.isString(path))
            throw new CorruptedValueException();
        return config.getString(path);
    }

    @Override
    default String format(String value) {
        return value;
    }

}
