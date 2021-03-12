package com.monst.bankingplugin.banking.bank.configuration;

import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.exceptions.DoubleParseException;
import com.monst.bankingplugin.utils.Utils;

import javax.annotation.Nonnull;
import java.text.NumberFormat;

public class InterestRate extends DoubleConfigurationOption {

    private static final NumberFormat formatter = NumberFormat.getInstance();
    static {
        formatter.setMinimumIntegerDigits(1);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(3);
    }

    public InterestRate() {
        super();
    }

    public InterestRate(Double value) {
        super(value);
    }

    @Override
    protected Config.ConfigPair<Double> getConfigPair() {
        return Config.getInterestRate();
    }

    @Override
    public String getFormatted() {
        return formatter.format(value);
    }

    @Override
    protected Double parse(@Nonnull String input) throws DoubleParseException {
        try {
            return Utils.scale(parseDouble(input), 3);
        } catch (NumberFormatException e) {
            throw new DoubleParseException(input);
        }
    }

}
