package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.pluginconfiguration.exception.ArgumentParseException;
import com.monst.pluginconfiguration.impl.BigDecimalConfigurationValue;
import com.monst.pluginconfiguration.validation.BigDecimalValidation;
import com.monst.pluginconfiguration.validation.Bound;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

/**
 * A {@link BigDecimalConfigurationValue} that represents a monetary value.
 */
public abstract class MonetaryConfigurationValue extends BigDecimalConfigurationValue {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();
    static {
        if (CURRENCY_FORMAT instanceof DecimalFormat)
            ((DecimalFormat) CURRENCY_FORMAT).setParseBigDecimal(true);
    }
    final BankingPlugin plugin;

    public MonetaryConfigurationValue(BankingPlugin plugin, String path, BigDecimal defaultValue) {
        super(plugin, path, defaultValue);
        this.plugin = plugin;
    }

    @Override
    public BigDecimal parse(String input) throws ArgumentParseException {
        if (!input.startsWith(CURRENCY_FORMAT.getCurrency().getSymbol()))
            return super.parse(input);
        try {
            Number number = CURRENCY_FORMAT.parse(input);
            if (number instanceof BigDecimal)
                return (BigDecimal) number;
            return BigDecimal.valueOf(number.doubleValue());
        } catch (ParseException e) {
            throw createArgumentParseException(input);
        }
    }

    @Override
    protected ArgumentParseException createArgumentParseException(String input) {
        return new ArgumentParseException(Message.NOT_A_NUMBER.with(Placeholder.INPUT).as(input).translate(plugin));
    }

    @Override
    protected List<Bound<BigDecimal>> getBounds() {
        return Arrays.asList(
                BigDecimalValidation.absolute(),
                BigDecimalValidation.scale(2)
        );
    }

}
