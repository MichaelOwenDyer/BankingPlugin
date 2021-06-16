package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.ArgumentParseException;
import org.bukkit.configuration.MemoryConfiguration;

public interface IConfigValue<T> {

    T parse(String input) throws ArgumentParseException;

    T readValueFromFile(MemoryConfiguration config, String path);

    default String format(T t) {
        return String.valueOf(t);
    }

    default Object convertToSettableType(T t) {
        return t;
    }

    boolean isPathMissing();

}
