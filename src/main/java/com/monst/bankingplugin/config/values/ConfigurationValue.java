package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.CorruptedValueException;
import com.monst.bankingplugin.exceptions.MissingValueException;
import com.monst.bankingplugin.exceptions.parse.ArgumentParseException;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.function.Supplier;

interface ConfigurationValue<V, T> extends Supplier<T> {

    T parse(String input) throws ArgumentParseException;

    T read(MemoryConfiguration config, String path) throws MissingValueException, CorruptedValueException;

    /**
     * Retrieves the raw object from the configuration file.
     */
    default Object get(MemoryConfiguration config, String path) {
        return config.get(path, null); // Use generic method get() to preserve nullability and ensure value presence
    }

    default V attemptConversion(Object o) throws CorruptedValueException {
        try {
            return convert(o);
        } catch (ClassCastException e) {
            throw new CorruptedValueException();
        }
    }

    /**
     * Casts the object into the correct type, throwing a ClassCastException if the object is not the correct type
     * or a CorruptedValueException with a replacement if the object is not the correct type, but can be repaired.
     * @throws ClassCastException if the object is of an unrelated type - the entry will be reset.
     * @throws CorruptedValueException if the object is of a related (but still incorrect) type - the entry will be repaired.
     */
    V convert(Object o) throws CorruptedValueException;

    default String format(T t) {
        return String.valueOf(t);
    }

    default Object convertToConfigType(T t) {
        return format(t);
    }

}
