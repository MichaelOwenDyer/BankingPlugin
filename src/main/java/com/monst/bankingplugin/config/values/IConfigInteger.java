package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.IntegerParseException;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.Optional;
import java.util.function.Function;

public interface IConfigInteger extends IConfigValue<Integer> {

    @Override
    default Integer readValueFromFile(MemoryConfiguration config, String path) {
        return config.getInt(path);
    }

    @Override
    default Integer parse(String input) throws IntegerParseException {
        return Optional.ofNullable(input)
                .map(Integer::parseInt)
                .map(getConstraint())
                .orElseThrow(() -> new IntegerParseException(input));
    }

    default Function<Integer, Integer> getConstraint() {
        return Function.identity();
    }

}
