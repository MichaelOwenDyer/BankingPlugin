package com.monst.bankingplugin.configuration.type;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.configuration.validation.Bound;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    public MonetaryConfigurationValue(BankingPlugin plugin, String path, BigDecimal defaultValue) {
        super(plugin, path, defaultValue);
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
            throw new ArgumentParseException(Message.NOT_A_NUMBER.with(Placeholder.INPUT).as(input));
        }
    }
    
    @Override
    protected List<Bound<BigDecimal>> getBounds() {
        return Arrays.asList(
                Bound.requiring(b -> b.signum() > 0, BigDecimal::abs),
                Bound.requiring(b -> b.scale() == 2, b -> b.setScale(2, RoundingMode.HALF_EVEN))
        );
    }
    
    @Override
    public String format(BigDecimal value) {
        if (plugin.getEconomy() == null) // Will be the case at startup
            return value.toString();
        return plugin.getEconomy().format(value.doubleValue());
    }

}
