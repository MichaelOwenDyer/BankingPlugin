package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.CorruptedValueException;
import com.monst.bankingplugin.exceptions.InvalidValueException;
import com.monst.bankingplugin.exceptions.MissingValueException;
import com.monst.bankingplugin.exceptions.parse.ArgumentParseException;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.function.Supplier;

/**
 * A configuration value stored in the config.yml file.
 * @param <V> the stored type of the value
 * @param <T> the actual type of the value
 */
interface ConfigurationValue<V, T> extends Supplier<T> {

    /**
     * Parses a new value using user input.
     * @param input a string representation of the desired object
     * @return the parsed object
     * @throws ArgumentParseException if the input could not be parsed
     */
    T parse(String input) throws ArgumentParseException;

    /**
     * Reads and reconstructs the value currently stored in the {@link MemoryConfiguration} under the given path.
     * @param config the config.yml file
     * @param path the path where the value is located
     * @return the reconstructed value currently stored in the config.yml file
     * @throws MissingValueException if the value is missing from the file
     * @throws InvalidValueException if the value is invalid but still readable
     * @throws CorruptedValueException if the value is invalid and not readable
     */
    T read(MemoryConfiguration config, String path)
            throws MissingValueException, InvalidValueException, CorruptedValueException;

    /**
     * Retrieves the raw object from the {@link MemoryConfiguration}.
     * By default this uses {@link MemoryConfiguration#get(String)} to preserve nullability, since many more specific
     * methods (e.g. {@link MemoryConfiguration#getInt(String)} return primitives.
     * This method is overridden where sensible.
     */
    default Object get(MemoryConfiguration config, String path) {
        return config.get(path, null); // Use generic method get() to preserve nullability and ensure value presence
    }

    /**
     * Throws a {@link MissingValueException} if the provided object from the configuration file is null.
     * @param o the object stored in the file
     * @throws MissingValueException if the value is missing from the file
     */
    default void ensurePresent(Object o) throws MissingValueException {
        if (o == null)
            throw new MissingValueException();
    }

    /**
     * Casts the object from the configuration file to the stored type of this value.
     * If a {@link ClassCastException} is thrown, it is wrapped in a {@link CorruptedValueException}.
     * @param o the object from the configuration file
     * @return the object and the correct stored type
     * @throws InvalidValueException if the object was of a related type and repairable
     * @throws CorruptedValueException if the object was of an unrelated type and not able to be interpreted
     */
    default V reconstructFromYaml(Object o) throws InvalidValueException, CorruptedValueException {
        try {
            return cast(o);
        } catch (ClassCastException e) {
            throw new CorruptedValueException();
        }
    }

    /**
     * Casts the object into the correct stored type, throwing a {@link ClassCastException} if the object is not
     * the correct type or an {@link InvalidValueException} if the object is not the correct type, but can be repaired.
     * @throws ClassCastException if the object is of an unrelated type - the entry will be reset.
     * @throws InvalidValueException if the object is of a related (but still incorrect) type - the entry will be repaired.
     */
    V cast(Object o) throws ClassCastException, InvalidValueException;

    /**
     * Throws an {@link InvalidValueException} if the reconstructed object is somehow outside its bounds, e.g.
     * an integer that should only be positive but is negative.
     * @param t the reconstructed object
     * @throws InvalidValueException if the object is outside its bounds
     */
    default void ensureValid(T t) throws InvalidValueException {}

    /**
     * Formats this configuration value to a human-readable string to be displayed to the user.
     * @param t the value
     * @return a pretty string
     */
    default String format(T t) {
        return String.valueOf(t);
    }

    default void write(MemoryConfiguration config, String path, Object o) {
        config.set(path, o);
    }

    /**
     * Converts a value to a different type to be stored back in the configuration file.
     * By default this returns the string representation of this value.
     * @param t the value
     * @return a storable object
     */
    default Object convertToStorableType(T t) {
        return format(t);
    }

}
