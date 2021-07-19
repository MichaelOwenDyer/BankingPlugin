package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.CorruptedValueException;
import com.monst.bankingplugin.exceptions.MissingValueException;
import org.bukkit.configuration.MemoryConfiguration;

interface IBinaryConfigValue<V, T> extends IConfigValue<V, T> {

    default T read(MemoryConfiguration config, String path) throws MissingValueException, CorruptedValueException {
        Object o = get(config, path);
        if (o == null)
            throw new MissingValueException();
        return convertToActualType(tryCast(o));
    }

    T convertToActualType(V v) throws CorruptedValueException;

    @Override
    default Object convertToConfigType(T t) {
        return format(t);
    }

}
