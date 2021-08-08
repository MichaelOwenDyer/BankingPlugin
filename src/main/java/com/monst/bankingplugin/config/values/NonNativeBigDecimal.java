package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.CorruptedValueException;
import com.monst.bankingplugin.exceptions.InvalidValueException;
import com.monst.bankingplugin.exceptions.parse.DecimalParseException;
import com.monst.bankingplugin.utils.Parser;
import com.monst.bankingplugin.utils.Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A BigDecimal configuration value stored non-natively as a double in the config.yml file.
 */
interface NonNativeBigDecimal extends NonNativeValue<Double, BigDecimal> {

    @Override
    default BigDecimal parse(String input) throws DecimalParseException {
        input = input.startsWith("$") ? input.substring(1) : input;
        return Parser.parseBigDecimal(input).setScale(2, RoundingMode.HALF_EVEN);
    }

    @Override
    default Double cast(Object o) {
        return ((Number) o).doubleValue();
    }

    @Override
    default BigDecimal translate(Double d) throws CorruptedValueException {
        try {
            return BigDecimal.valueOf(d);
        } catch (NumberFormatException e) {
            throw new CorruptedValueException();
        }
    }

    @Override
    default String format(BigDecimal value) {
        return Utils.format(value);
    }

    @Override
    default Object convertToStorableType(BigDecimal bd) {
        return bd.doubleValue();
    }

    @Override
    default void ensureValid(BigDecimal bd) throws InvalidValueException {
        if (bd.scale() > 2)
            throw new InvalidValueException(bd.setScale(2, RoundingMode.HALF_EVEN));
    }

    interface Absolute extends NonNativeBigDecimal {
        @Override
        default void ensureValid(BigDecimal bd) throws InvalidValueException {
            if (bd.signum() < 0)
                throw new InvalidValueException(bd.abs().setScale(2, RoundingMode.HALF_EVEN));
        }
    }

}
