package com.monst.bankingplugin.config.values;

public interface IConfigString extends IUnaryConfigValue<String> {

    @Override
    default String parse(String input) {
        return input;
    }

    @Override
    default boolean isCorrectType(Object o) {
        return o instanceof String;
    }

    @Override
    default String format(String value) {
        return value;
    }

}
