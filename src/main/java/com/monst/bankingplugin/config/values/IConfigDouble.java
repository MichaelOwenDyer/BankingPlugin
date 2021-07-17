package com.monst.bankingplugin.config.values;

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
    default boolean isCorrectType(Object o) {
        return o instanceof Double;
    }

    @Override
    default String format(Double value) {
        return Utils.format(value);
    }

    interface Absolute extends IConfigDouble {
        @Override
        default boolean isCorrupted(Double d) {
            return d < 0;
        }
        @Override
        default Double replace(Double d) {
            return Math.abs(d);
        }
    }

}
