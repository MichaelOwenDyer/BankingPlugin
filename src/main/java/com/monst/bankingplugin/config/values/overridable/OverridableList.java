package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.exceptions.ListParseException;
import org.bukkit.configuration.file.FileConfiguration;

import javax.annotation.Nonnull;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class OverridableList<T> extends OverridableValue<List<T>> {

    OverridableList(String path, List<T> defaultValue, BiFunction<FileConfiguration, String, List<T>> valueFinder) {
        super(path, defaultValue, valueFinder);
    }

    @Override
    public OverriddenValue<List<T>> override(List<T> value) {
        return new OverriddenList<>(this, value);
    }

    @Override
    public List<T> parse(@Nonnull String input) throws ListParseException {
        try {
            return parseToStream(input).collect(Collectors.toList());
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new ListParseException(input);
        }
    }

    abstract Stream<T> parseToStream(String input);

}
