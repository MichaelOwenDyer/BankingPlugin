package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.CorruptedValueException;
import com.monst.bankingplugin.exceptions.InvalidValueException;
import com.monst.bankingplugin.exceptions.parse.ArgumentParseException;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A collection of objects stored in the config.yml file as a {@link List<String>}.
 * @param <T> the type of object
 * @param <C> the type of collection
 */
interface ConfigCollection<T, C extends Collection<T>> extends NonNativeValue<List<String>, C> {

    /**
     * Parses a collection of objects from an input string.
     * The input string is split over any amount of spaces with or without a comma inside.
     * Every split string is parsed into an object, validated, and added to a collection.
     * If a string cannot be parsed, the process is aborted with an {@link ArgumentParseException}.
     * If an object is validated and deemed to be outside its bounds, its replacement is used instead.
     * @param input a string representation of the desired collection
     * @return a collection of parsed and validated values
     * @throws ArgumentParseException if a string could not be parsed
     */
    @Override
    default C parse(String input) throws ArgumentParseException {
        C collection = getEmptyCollection();
        for (String s : input.split("\\s*(,|\\s)\\s*")) { // the entered list may be separated by commas or spaces or both
            try {
                T t = parseSingle(s);
                ensureValidSingle(t);
                collection.add(t);
            } catch (InvalidValueException e) {
                collection.add(e.getValidatedValue());
            }
        }
        return collection;
    }

    /**
     * Retrieves a string list from the {@link MemoryConfiguration}, if and only if the path exists.
     * @return a list of strings that can be parsed
     * @see #translate(List)
     */
    @Override
    default Object get(MemoryConfiguration config, String path) {
        if (config.contains(path, true)) // Must check whether path is present to preserve nullability
            return config.getStringList(path);
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    default List<String> cast(Object o) {
        return (List<String>) o;
    }

    /**
     * Parses a collection of objects from a list of strings in the configuration file.
     * Each string is parsed into an object, validated, and added to a collection.
     * In contrast to {@link #parse(String)}, {@link ArgumentParseException}s will not interrupt the process.
     * If any problems are found while parsing, validating, and adding the objects to the collection, this will be
     * recorded in {@code problemFound} and dealt with later.
     * If a problem was found, an exception will be thrown.
     * If anything of use was reconstructed, this exception will be an {@link InvalidValueException} with the
     * collection as a replacement, and the configuration file will be updated to match this collection.
     * If nothing of use could be reconstructed, a {@link CorruptedValueException} will be thrown and the value
     * in the configuration file will be reset to default.
     * @param strings a list of strings from the configuration file
     * @return a collection of parsed and validated values
     * @throws InvalidValueException if there was a problem with at least one string in the configuration file.
     * @throws CorruptedValueException if there was a problem with all strings in the configuration file.
     */
    @Override
    default C translate(List<String> strings) throws InvalidValueException, CorruptedValueException {
        C collection = getEmptyCollection();
        boolean problemFound = false; // Whether the value needs to be updated in the config file
        for (String s : strings) {
            try {
                T t = parseSingle(s); // Parse the string from the config
                ensureValidSingle(t); // Ensure parsed object is within its bounds
                if (!collection.add(t)) // Add parsed object to the collection
                    problemFound = true; // Parsed object could not be added because it violated a collection constraint
            } catch (ArgumentParseException e) { // String from the config could not be parsed
                problemFound = true; // Nothing is added to the collection
            } catch (InvalidValueException e) { // Parsed object was outside its bounds
                if (!collection.add(e.getValidatedValue())) // Add the replacement to the collection instead
                    problemFound = true; // Replacement could not be added because it violated a collection constraint
            }
        }
        if (problemFound) { // Config needs to be updated
            if (collection.isEmpty()) // Nothing usable was found in the config
                throw new CorruptedValueException(); // Set the value to default in the config
            throw new InvalidValueException(collection); // Something usable was found, put it in the config file
        }
        return collection;
    }

    C getEmptyCollection();

    T parseSingle(String input) throws ArgumentParseException;

    default void ensureValidSingle(T t) throws InvalidValueException {}

    default String formatSingle(T t) {
        return String.valueOf(t);
    }

    @Override
    default String format(C collection) {
        if (collection.isEmpty())
            return "[]";
        return collection.stream().map(this::formatSingle).collect(Collectors.joining(", ")); // do not include [ ]
    }

    @Override
    default Object convertToStorableType(C collection) {
        return collection.stream().map(this::formatSingle).collect(Collectors.toList());
    }

}
