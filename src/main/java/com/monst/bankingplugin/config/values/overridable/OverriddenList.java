package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.exceptions.ListParseException;

import javax.annotation.Nonnull;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class OverriddenList<T> extends OverriddenValue<List<T>> {

    OverriddenList(OverridableList<T> attribute, List<T> value) {
        super(attribute, value);
    }

    @Override
    List<T> parse(@Nonnull String input) throws ListParseException {
        try {
            return parseToStream(input).collect(Collectors.toList());
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new ListParseException(input);
        }
    }

    abstract Stream<T> parseToStream(String input);

}
