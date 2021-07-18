package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.CorruptedValueException;
import com.monst.bankingplugin.exceptions.parse.DoubleParseException;
import com.monst.bankingplugin.utils.Parser;
import com.monst.bankingplugin.utils.QuickMath;
import com.monst.bankingplugin.utils.Utils;

public interface IConfigDouble extends IUnaryConfigValue<Double> {

    @Override
    default Double parse(String input) throws DoubleParseException {
        return QuickMath.scale(Parser.parseDouble(input));
    }

    @Override
    default Double cast(Object o) throws CorruptedValueException {
        if (!(o instanceof Number))
            throw new CorruptedValueException();
        return ((Number) o).doubleValue();
    }

    @Override
    default String format(Double value) {
        return Utils.format(value);
    }

    interface Absolute extends IConfigDouble {
        @Override
        default void ensureValid(Double d) throws CorruptedValueException {
            if (d < 0)
                throw new CorruptedValueException(Math.abs(d));
        }
    }

}
