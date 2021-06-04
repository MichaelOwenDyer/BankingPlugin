package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.ArgumentParseException;

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

    C getEmptyCollection();

    T parseSingle(String input) throws ArgumentParseException;

    @Override
    default String format(C collection) {
        return collection.stream().map(String::valueOf).collect(Collectors.joining(", ")); // do not include [ ]
    }

}