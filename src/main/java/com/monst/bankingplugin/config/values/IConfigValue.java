package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.CorruptedValueException;
import com.monst.bankingplugin.exceptions.parse.ArgumentParseException;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.function.Supplier;

public interface IConfigValue<T> extends Supplier<T> {

    T parse(String input) throws ArgumentParseException;

    T readFromFile(MemoryConfiguration config, String path) throws CorruptedValueException;

    default String format(T t) {
        return String.valueOf(t);
    }

    default Object convertToSettableType(T t) {
        return t;
    }

}
