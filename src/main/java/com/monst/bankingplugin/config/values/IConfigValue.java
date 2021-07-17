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
        if (!isCorrectType(o))
            throw new CorruptedValueException();
        @SuppressWarnings("unchecked")
        V v = (V) o;
        return convertToActualType(v);
    }

    default Object get(MemoryConfiguration config, String path) {
        return config.get(path, null);
    }

    boolean isCorrectType(Object o);

    T convertToActualType(V v) throws CorruptedValueException;

    default String format(T t) {
        return String.valueOf(t);
    }

    default Object convertToConfigType(T t) {
        return format(t);
    }

}
