package com.monst.bankingplugin.banking.bank.configuration;

import com.monst.bankingplugin.exceptions.IntegerParseException;

import javax.annotation.Nonnull;

abstract class IntegerConfigurationOption extends ConfigurationOption<Integer> {

    protected IntegerConfigurationOption() {
        super();
    }

    protected IntegerConfigurationOption(Integer value) {
        super(value);
    }

    @Override
    protected Integer parse(@Nonnull String input) throws IntegerParseException {
        try {
            return Math.abs(Integer.parseInt(input));
        } catch (NumberFormatException e) {
            throw new IntegerParseException(input);
        }
    }

}
