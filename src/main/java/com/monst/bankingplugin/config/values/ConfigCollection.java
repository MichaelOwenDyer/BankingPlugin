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

    @Override
    default C parse(String input) throws ArgumentParseException {
        C collection = getEmptyCollection();
        for (String s : input.split("\\s*(,|\\s)\\s*")) { // the entered list may be separated by commas or spaces or both
            try {
                T t = parseSingle(s);
                ensureValidSingle(t);
                collection.add(t);
            } catch (InvalidValueException e) {
                collection.add(e.getReplacement());
            }
        }
        return collection;
    }

    @Override
    default Object get(MemoryConfiguration config, String path) {
        return config.get(path, null) != null ? config.getStringList(path) : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    default List<String> cast(Object o) {
        return (List<String>) o;
    }

    @Override
    default C translate(List<String> strings) throws InvalidValueException, CorruptedValueException {
        C collection = getEmptyCollection();
        boolean isCorrupted = false;
        for (String s : strings) {
            try {
                T t = parseSingle(s);
                ensureValidSingle(t);
                if (!collection.add(t))
                    isCorrupted = true; // if object could not be added because it violated a constraint
            } catch (ArgumentParseException e) {
                isCorrupted = true;
            } catch (InvalidValueException e) {
                if (!collection.add(e.getReplacement()))
                    isCorrupted = true;
            }
        }
        if (isCorrupted) {
            if (collection.isEmpty())
                throw new CorruptedValueException();
            throw new InvalidValueException(collection); // Suggest replacement value for config
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
