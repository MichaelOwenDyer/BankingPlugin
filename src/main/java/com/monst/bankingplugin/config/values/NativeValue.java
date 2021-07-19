package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.CorruptedValueException;
import com.monst.bankingplugin.exceptions.InvalidValueException;
import com.monst.bankingplugin.exceptions.MissingValueException;
import org.bukkit.configuration.MemoryConfiguration;

/**
 * A configuration value stored natively in the config.yml file.
 * @param <T> the type of the value
 */
interface NativeValue<T> extends ConfigurationValue<T, T> {

    @Override
    default T read(MemoryConfiguration config, String path) throws MissingValueException, InvalidValueException, CorruptedValueException {
        Object o = get(config, path);
        ensurePresent(o);
        T t = reconstructFromYaml(o);
        ensureValid(t);
        return t;
    }

    @Override
    default Object convertToStorableType(T t) {
        return t;
    }

}
