package com.monst.bankingplugin.banking.bank.configuration;

import com.monst.bankingplugin.exceptions.ListParseException;

import javax.annotation.Nonnull;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class ListConfigurationOption<T> extends ConfigurationOption<List<T>> {

    ListConfigurationOption() {
        super();
    }

    ListConfigurationOption(List<T> value) {
        super(value);
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
