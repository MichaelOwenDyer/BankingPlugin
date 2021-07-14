package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.CorruptedValueException;
import com.monst.bankingplugin.exceptions.parse.BooleanParseException;
import com.monst.bankingplugin.utils.Parser;
import org.bukkit.configuration.MemoryConfiguration;

public interface IConfigBoolean extends IConfigValue<Boolean> {

    @Override
    default Boolean parse(String input) throws BooleanParseException {
        return Parser.parseBoolean(input);
    }

    @Override
    default Boolean readFromFile(MemoryConfiguration config, String path) throws CorruptedValueException {
        if (!config.isBoolean(path))
            throw new CorruptedValueException();
        return config.getBoolean(path);
    }

}
