package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.CorruptedValueException;

public interface IUnaryConfigValue<T> extends IConfigValue<T, T> {

    default T convertToActualType(T t) throws CorruptedValueException {
        ensureValid(t);
        return t;
    }

    default void ensureValid(T t) throws CorruptedValueException {}

    default Object convertToConfigType(T t) { return t; }

}
