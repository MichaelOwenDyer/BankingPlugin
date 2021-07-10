package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.DoubleParseException;
import com.monst.bankingplugin.utils.QuickMath;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.function.Function;

public interface IConfigDouble extends IConfigValue<Double> {

    @Override
    default Double parse(String input) throws DoubleParseException {
        return getConstraint().apply(QuickMath.scale(Utils.parseDouble(Utils.removePunctuation(input, '.'))));
    }

    @Override
    default Double readFromFile(MemoryConfiguration config, String path) {
        return config.getDouble(path);
    }

    default Function<Double, Double> getConstraint() {
        return Function.identity();
    }

    @Override
    default String format(Double value) {
        return Utils.format(value);
    }

}
