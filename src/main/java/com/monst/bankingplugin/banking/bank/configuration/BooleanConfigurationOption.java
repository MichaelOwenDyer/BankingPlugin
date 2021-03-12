package com.monst.bankingplugin.banking.bank.configuration;

import javax.annotation.Nonnull;

abstract class BooleanConfigurationOption extends ConfigurationOption<Boolean> {

    BooleanConfigurationOption() {
        super();
    }

    BooleanConfigurationOption(Boolean value) {
        super(value);
    }

    @Override
    Boolean parse(@Nonnull String input) {
        return Boolean.parseBoolean(input);
    }

}
