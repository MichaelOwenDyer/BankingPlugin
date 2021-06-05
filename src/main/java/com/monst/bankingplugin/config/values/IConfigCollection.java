package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.ArgumentParseException;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.Collection;
import java.util.stream.Collectors;

public interface IConfigCollection<T, C extends Collection<T>> extends IConfigValue<C> {

    @Override
    default C parse(String input) throws ArgumentParseException {
        C collection = getEmptyCollection();
        for (String string : input.split("\\s*(,|\\s)\\s*")) // the entered list may be separated by commas or spaces or both
            collection.add(parseSingle(string));
        return collection;
    }

    @Override
    default C readValueFromFile(MemoryConfiguration config, String path) {
        if (isPathMissing())
            return null;
        C collection = getEmptyCollection();
        for (String string : config.getStringList(path))
            try {
                collection.add(parseSingle(string));
            } catch (Exception ignored) {}
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

}
