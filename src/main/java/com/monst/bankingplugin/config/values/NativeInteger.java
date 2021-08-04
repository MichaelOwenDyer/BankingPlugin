package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.InvalidValueException;
import com.monst.bankingplugin.exceptions.parse.IntegerParseException;
import com.monst.bankingplugin.utils.Parser;

interface NativeInteger extends NativeValue<Integer> {

    @Override
    default Integer parse(String input) throws IntegerParseException {
        return Parser.parseInt(input);
    }

    @Override
    default Integer cast(Object o) throws InvalidValueException {
        Number n = (Number) o;
        if (!(n instanceof Integer))
            throw new InvalidValueException(n.intValue());
        return (Integer) n;
    }

    interface Absolute extends NativeInteger {
        @Override
        default void ensureValid(Integer i) throws InvalidValueException {
            if (i < 0)
                throw new InvalidValueException(Math.abs(i));
        }
    }

}
