package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.IntegerParseException;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.Optional;
import java.util.function.Function;

public interface IConfigInteger extends IConfigValue<Integer> {

    @Override
    default Integer parse(String input) throws IntegerParseException {
        return Optional.ofNullable(input)
                .map(Utils::parseInteger)
                .map(getConstraint())
                .orElseThrow(() -> new IntegerParseException(input));
    }

    @Override
    default Integer readValueFromFile(MemoryConfiguration config, String path) {
        if (isPathMissing())
            return null;
        return config.getInt(path);
    }

    default Function<Integer, Integer> getConstraint() {
        return Function.identity();
    }

}
