package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.ListParseException;

import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public interface IConfigList<T> extends IConfigValue<List<T>> {

    @Override
    default List<T> parse(String input) throws ListParseException {
        try {
            return Arrays.stream(input.split("\\s*,?\\s*"))
                    .map(this::parseSingle)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new ListParseException(input);
        }
    }

    T parseSingle(String input);

}
