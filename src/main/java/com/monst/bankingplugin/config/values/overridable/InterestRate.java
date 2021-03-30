package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.exceptions.DoubleParseException;
import com.monst.bankingplugin.utils.QuickMath;
import com.monst.bankingplugin.utils.Utils;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.text.NumberFormat;

public class InterestRate extends OverridableDouble {

    private static final NumberFormat FORMATTER = NumberFormat.getInstance();
    static {
        FORMATTER.setMinimumIntegerDigits(1);
        FORMATTER.setMinimumFractionDigits(1);
        FORMATTER.setMaximumFractionDigits(2);
    }

    public InterestRate() {
        super("interest-rate", 0.01);
    }

    @Override
    public OverriddenValue<Double> override(Double value) {
        return new OverriddenDouble(this, value) {
            @Override
            public String format(Double value) {
                return FORMATTER.format(value * 100) + "%";
            }

            @Override
            Double parse(@Nonnull String input) throws DoubleParseException {
                BigDecimal bd;
                try {
                    bd = new BigDecimal(Utils.removePunctuation(input, '.')).abs();
                } catch (NumberFormatException e) {
                    throw new DoubleParseException(input);
                }
                bd = QuickMath.scale(bd, 4);
                if (input.charAt(input.length() - 1) == '%')
                    bd = QuickMath.divide(bd, 100);
                return bd.doubleValue();
            }
        };
    }

}
