package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.CorruptedValueException;
import com.monst.bankingplugin.exceptions.parse.IntegerParseException;
import com.monst.bankingplugin.utils.Parser;

interface IConfigInteger extends IUnaryConfigValue<Integer> {

    @Override
    default Integer parse(String input) throws IntegerParseException {
        return Parser.parseInt(input);
    }

    @Override
    default Integer cast(Object o) throws CorruptedValueException {
        if (!(o instanceof Number))
            throw new CorruptedValueException();
        if (!(o instanceof Integer)) // Value is a number, but not an integer
            throw new CorruptedValueException(((Number) o).intValue()); // Repair it
        return (Integer) o;
    }

    interface Absolute extends IConfigInteger {
        @Override
        default void ensureValid(Integer i) throws CorruptedValueException {
            if (i < 0)
                throw new CorruptedValueException(Math.abs(i));
        }
    }

}
