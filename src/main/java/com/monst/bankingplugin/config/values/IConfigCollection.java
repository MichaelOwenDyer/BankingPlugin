package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.CorruptedValueException;
import com.monst.bankingplugin.exceptions.parse.ArgumentParseException;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A collection of objects stored in the config as a {@link List<String>}.
 * @param <T> the type of object
 * @param <C> the type of collection
 */
interface IConfigCollection<T, C extends Collection<T>> extends NonNativeValue<List<String>, C> {

    @Override
    default C parse(String input) throws ArgumentParseException {
        C collection = getEmptyCollection();
        for (String string : input.split("\\s*(,|\\s)\\s*")) // the entered list may be separated by commas or spaces or both
            collection.add(parseSingle(string));
        return collection;
    }

    @Override
    default Object get(MemoryConfiguration config, String path) {
        return config.get(path, null) != null ? config.getStringList(path) : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    default List<String> convert(Object o) {
        return (List<String>) o;
    }

    @Override
    default C convertToActualType(List<String> vs) throws CorruptedValueException {
        C collection = getEmptyCollection();
        boolean isCorrupted = false;
        for (String v : vs)
            try {
                if (!collection.add(parseSingle(v)))
                    isCorrupted = true; // if object could not be added because it violated a collection constraint
            } catch (ArgumentParseException e) {
                isCorrupted = true; // if object could not be parsed
            }
        if (isCorrupted) {
            if (collection.isEmpty())
                collection = null;
            throw new CorruptedValueException(collection); // Suggest replacement value for config
        }
        return collection;
    }

    C getEmptyCollection();

    T parseSingle(String input) throws ArgumentParseException;

    @Override
    default String format(C collection) {
        if (collection.isEmpty())
            return "[]";
        return collection.stream().map(String::valueOf).collect(Collectors.joining(", ")); // do not include [ ]
    }

    @Override
    default Object convertToConfigType(C ts) {
        return ts;
    }

}
