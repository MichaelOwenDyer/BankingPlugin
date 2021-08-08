package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.CorruptedValueException;
import com.monst.bankingplugin.exceptions.parse.ArgumentParseException;

/**
 * A configuration value stored non-natively as a String in the config.yml file.
 * @param <T> the actual type of the value
 */
interface NonNativeString<T> extends NonNativeValue<String, T> {

    @Override
    default String cast(Object o) {
        return o.toString();
    }

    default T translate(String s) throws CorruptedValueException {
        try {
            return parse(s);
        } catch (ArgumentParseException e) {
            throw new CorruptedValueException();
        }
    }

}
