package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.parse.IntegerParseException;
import com.monst.bankingplugin.utils.Parser;

public interface IConfigInteger extends IUnaryConfigValue<Integer> {

    @Override
    default Integer parse(String input) throws IntegerParseException {
        return Parser.parseInt(input);
    }

    @Override
    default boolean isCorrectType(Object o) {
        return o instanceof Integer;
    }

    interface Absolute extends IConfigInteger {
        @Override
        default boolean isCorrupted(Integer i) {
            return i < 0;
        }
        @Override
        default Integer replace(Integer d) {
            return Math.abs(d);
        }
    }

}
