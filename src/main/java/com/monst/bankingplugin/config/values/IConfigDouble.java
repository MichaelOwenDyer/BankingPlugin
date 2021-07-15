package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.CorruptedValueException;
import com.monst.bankingplugin.exceptions.parse.DoubleParseException;
import com.monst.bankingplugin.utils.Parser;
import com.monst.bankingplugin.utils.QuickMath;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.configuration.MemoryConfiguration;

public interface IConfigDouble extends IConfigValue<Double> {

    @Override
    default Double parse(String input) throws DoubleParseException {
        return constrain(QuickMath.scale(Parser.parseDouble(input)));
    }

    @Override
    default Double readFromFile(MemoryConfiguration config, String path) throws CorruptedValueException {
        if (!config.isDouble(path))
            throw new CorruptedValueException();
        return config.getDouble(path);
    }

    default Double constrain(Double d) {
        return d;
    }

    @Override
    default String format(Double value) {
        return Utils.format(value);
    }

    interface Absolute extends IConfigDouble {
        @Override
        default Double constrain(Double d) {
            return Math.abs(d);
        }
    }

}
