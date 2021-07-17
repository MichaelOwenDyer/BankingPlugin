package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.CorruptedValueException;

public interface IUnaryConfigValue<T> extends IConfigValue<T, T> {

    default T convertToActualType(T t) throws CorruptedValueException {
        if (isCorrupted(t))
            throw new CorruptedValueException(replace(t));
        return t;
    }

    default boolean isCorrupted(T t) {
        return false;
    }

    default T replace(T t) {
        return null;
    }

    default Object convertToConfigType(T t) {
        return t;
    }

}
