package com.monst.bankingplugin.config.values.overridable;

import com.monst.bankingplugin.exceptions.IntegerParseException;
import org.bukkit.configuration.MemoryConfiguration;

import javax.annotation.Nonnull;
import java.util.Optional;

abstract class OverridableInteger extends OverridableValue<Integer> {

    OverridableInteger(String path, Integer defaultValue) {
        super(path, defaultValue, MemoryConfiguration::getInt);
    }

    @Override
    public Integer parse(@Nonnull String input) throws IntegerParseException {
        return Optional.ofNullable(input)
                .map(Integer::parseInt)
                .map(Math::abs)
                .orElseThrow(() -> new IntegerParseException(input));
    }

}
