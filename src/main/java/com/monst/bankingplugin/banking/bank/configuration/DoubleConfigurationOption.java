package com.monst.bankingplugin.banking.bank.configuration;

import com.monst.bankingplugin.exceptions.DoubleParseException;
import com.monst.bankingplugin.utils.QuickMath;
import com.monst.bankingplugin.utils.Utils;

import javax.annotation.Nonnull;

abstract class DoubleConfigurationOption extends ConfigurationOption<Double> {

    DoubleConfigurationOption() {
        super();
    }

    DoubleConfigurationOption(Double value) {
        super(value);
    }

    @Override
    public String format(Double value) {
        return Utils.format(value);
    }

    @Override
    Double parse(@Nonnull String input) throws DoubleParseException {
        try {
            return QuickMath.scale(parseDouble(input));
        } catch (NumberFormatException e) {
            throw new DoubleParseException(input);
        }
    }

    static double parseDouble(@Nonnull String input) {
        return Math.abs(Double.parseDouble(Utils.removePunctuation(input, '.')));
    }

}
