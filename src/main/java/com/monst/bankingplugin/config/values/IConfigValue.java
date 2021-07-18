package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.CorruptedValueException;
import com.monst.bankingplugin.exceptions.MissingValueException;
import com.monst.bankingplugin.exceptions.parse.ArgumentParseException;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.function.Supplier;

public interface IConfigValue<V, T> extends Supplier<T> {

    T parse(String input) throws ArgumentParseException;

    default T read(MemoryConfiguration config, String path) throws MissingValueException, CorruptedValueException {
        Object o = get(config, path);
        if (o == null)
            throw new MissingValueException();
        try {
            return convertToActualType(cast(o));
        } catch (ClassCastException e) {
            throw new CorruptedValueException();
        }
    }

    /**
     * Retrieves the raw object from the configuration file.
     */
    default Object get(MemoryConfiguration config, String path) {
        return config.get(path, null); // Use generic method get() to preserve nullability and ensure value presence
    }

    /**
     * Casts the object into the correct type, throwing a ClassCastException if the object is not the correct type
     * or a CorruptedValueException with a replacement if the object is not the correct type, but can be repaired.
     * @throws ClassCastException if the object is of an unrelated type - the entry will be reset.
     * @throws CorruptedValueException if the object is of a related (but still incorrect) type - the entry will be repaired.
     */
    @SuppressWarnings("unchecked")
    default V cast(Object o) throws CorruptedValueException {
        return (V) o;
    }

    T convertToActualType(V v) throws CorruptedValueException;

    default String format(T t) {
        return String.valueOf(t);
    }

    default Object convertToConfigType(T t) {
        return format(t);
    }

}
