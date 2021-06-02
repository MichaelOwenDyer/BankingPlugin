package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.DoubleParseException;
import com.monst.bankingplugin.utils.QuickMath;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.Optional;
import java.util.function.Function;

public interface IConfigDouble extends IConfigValue<Double> {

    @Override
    default Double readValueFromFile(MemoryConfiguration config, String path) {
        return config.getDouble(path);
    }

    @Override
    default Double parse(String input) throws DoubleParseException {
        return Optional.ofNullable(input)
                .map(i -> Utils.removePunctuation(i, '.'))
                .map(Double::parseDouble)
                .map(getConstraint())
                .map(QuickMath::scale)
                .orElseThrow(() -> new DoubleParseException(input));
    }

    default Function<Double, Double> getConstraint() {
        return Function.identity();
    }

}
