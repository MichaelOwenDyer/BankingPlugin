package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.InvalidValueException;
import com.monst.bankingplugin.exceptions.parse.DoubleParseException;
import com.monst.bankingplugin.utils.Parser;
import com.monst.bankingplugin.utils.QuickMath;
import com.monst.bankingplugin.utils.Utils;

interface NativeDouble extends NativeValue<Double> {

    @Override
    default Double parse(String input) throws DoubleParseException {
        input = input.startsWith("$") ? input.substring(1) : input;
        return QuickMath.scale(Parser.parseDouble(input));
    }

    @Override
    default Double cast(Object o) {
        return ((Number) o).doubleValue();
    }

    @Override
    default String format(Double value) {
        return Utils.format(value);
    }

    interface Absolute extends NativeDouble {
        @Override
        default void ensureValid(Double d) throws InvalidValueException {
            if (d < 0)
                throw new InvalidValueException(Math.abs(d));
        }
    }

}
