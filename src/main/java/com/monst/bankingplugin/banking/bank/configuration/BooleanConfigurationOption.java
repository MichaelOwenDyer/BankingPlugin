package com.monst.bankingplugin.banking.bank.configuration;

import javax.annotation.Nonnull;

abstract class BooleanConfigurationOption extends ConfigurationOption<Boolean> {

    protected BooleanConfigurationOption() {
        super();
    }

    protected BooleanConfigurationOption(Boolean value) {
        super(value);
    }

    @Override
    protected Boolean parse(@Nonnull String input) {
        return Boolean.parseBoolean(input);
    }

}
