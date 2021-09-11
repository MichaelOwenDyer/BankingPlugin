package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.parse.BooleanParseException;
import com.monst.bankingplugin.utils.Parser;

interface NativeBoolean extends NativeValue<Boolean> {

    @Override
    default Boolean parse(String input) throws BooleanParseException {
        return Parser.parseBoolean(input);
    }

    @Override
    default Boolean cast(Object o) throws ClassCastException {
        return (Boolean) o;
    }

}
