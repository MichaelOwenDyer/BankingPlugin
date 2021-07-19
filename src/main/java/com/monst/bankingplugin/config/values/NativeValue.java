package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.CorruptedValueException;
import com.monst.bankingplugin.exceptions.MissingValueException;
import org.bukkit.configuration.MemoryConfiguration;

interface NativeValue<T> extends ConfigurationValue<T, T> {

    @Override
    default T read(MemoryConfiguration config, String path) throws MissingValueException, CorruptedValueException {
        Object o = get(config, path);
        if (o == null)
            throw new MissingValueException();
        T t = attemptConversion(o);
        ensureValid(t);
        return t;
    }

    default void ensureValid(T t) throws CorruptedValueException {}

}
