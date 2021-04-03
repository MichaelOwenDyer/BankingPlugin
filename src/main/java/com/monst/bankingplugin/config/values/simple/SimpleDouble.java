package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.exceptions.DoubleParseException;
import com.monst.bankingplugin.utils.QuickMath;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.configuration.MemorySection;

import java.util.Optional;
import java.util.function.Function;

public abstract class SimpleDouble extends SimpleValue<Double> {

    public SimpleDouble(String path, Double defaultValue) {
        super(path, defaultValue, MemorySection::getDouble); // TODO: Apply constraint
    }

    @Override
    public Double parse(String input) throws DoubleParseException {
        return Optional.ofNullable(input)
                .map(i -> Utils.removePunctuation(i, '.'))
                .map(Double::parseDouble)
                .map(getConstraint())
                .map(QuickMath::scale)
                .orElseThrow(() -> new DoubleParseException(input));
    }

    Function<Double, Double> getConstraint() {
        return Function.identity();
    }

}
