package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.parse.BooleanParseException;
import com.monst.bankingplugin.utils.Parser;

public interface IConfigBoolean extends IUnaryConfigValue<Boolean> {

    @Override
    default Boolean parse(String input) throws BooleanParseException {
        return Parser.parseBoolean(input);
    }

    @Override
    default Boolean cast(Object o) {
        return (Boolean) o;
    }

}
