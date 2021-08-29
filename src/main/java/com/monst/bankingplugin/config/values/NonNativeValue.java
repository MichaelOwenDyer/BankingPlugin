package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.CorruptedValueException;
import com.monst.bankingplugin.exceptions.InvalidValueException;
import com.monst.bankingplugin.exceptions.MissingValueException;
import org.bukkit.configuration.MemoryConfiguration;

/**
 * A configuration value stored non-natively in the config.yml file.
 * The value must be stored in a different type than its own and then translated back.
 * @param <V> the stored type of the value
 * @param <T> the actual type of the value
 */
interface NonNativeValue<V, T> extends ConfigurationValue<V, T> {

    default T read(MemoryConfiguration config, String path) throws MissingValueException, InvalidValueException, CorruptedValueException {
        Object o = get(config, path);
        ensurePresent(o);
        V v = reconstructFromYaml(o);
        T t = translate(v);
        ensureValid(t);
        return t;
    }

    T translate(V v) throws InvalidValueException, CorruptedValueException;

}
