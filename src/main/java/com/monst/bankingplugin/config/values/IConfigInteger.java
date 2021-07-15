package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.CorruptedValueException;
import com.monst.bankingplugin.exceptions.parse.IntegerParseException;
import com.monst.bankingplugin.utils.Parser;
import org.bukkit.configuration.MemoryConfiguration;

public interface IConfigInteger extends IConfigValue<Integer> {

    @Override
    default Integer parse(String input) throws IntegerParseException {
        return constrain(Parser.parseInt(input));
    }

    @Override
    default Integer readFromFile(MemoryConfiguration config, String path) throws CorruptedValueException {
        if (!config.isInt(path))
            throw new CorruptedValueException();
        return config.getInt(path);
    }

    default Integer constrain(Integer i) {
        return i;
    }

    interface Absolute extends IConfigInteger {
        @Override
        default Integer constrain(Integer i) {
            return Math.abs(i);
        }
    }

}
