package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.exceptions.ListParseException;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.format.DateTimeParseException;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class SimpleSet<T> extends SimpleConfigValue<Set<T>> {

    public SimpleSet(String path, Set<T> defaultValue, BiFunction<FileConfiguration, String, Set<T>> valueFinder) {
        super(path, defaultValue, valueFinder);
    }

    @Override
    Set<T> parse(String input) throws ListParseException {
        try {
            return parseToStream(input).collect(Collectors.toSet());
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new ListParseException(input);
        }
    }

    abstract Stream<T> parseToStream(String input) throws ListParseException;

}
