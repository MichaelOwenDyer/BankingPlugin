package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.exceptions.IntegerParseException;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Optional;
import java.util.function.Function;

abstract class SimpleInteger extends SimpleConfigValue<Integer> {

    public SimpleInteger(String path, Integer defaultValue) {
        super(path, defaultValue, FileConfiguration::getInt);
    }

    @Override
    Integer parse(String input) throws IntegerParseException {
        return Optional.ofNullable(input)
                .map(Integer::parseInt)
                .map(getConstraint())
                .orElseThrow(() -> new IntegerParseException(input));
    }

    Function<Integer, Integer> getConstraint() {
        return Function.identity();
    }

}
