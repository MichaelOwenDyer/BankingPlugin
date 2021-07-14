package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.config.values.ConfigValue;
import com.monst.bankingplugin.exceptions.CorruptedValueException;
import com.monst.bankingplugin.exceptions.parse.PatternParseException;
import com.monst.bankingplugin.utils.Parser;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.regex.Pattern;

public class NameRegex extends ConfigValue<Pattern> {

    public NameRegex() {
        super("name-regex", Pattern.compile(".*"));
    }

    @Override
    public Pattern parse(String input) throws PatternParseException {
        return Parser.parsePattern(input);
    }

    @Override
    public Pattern readFromFile(MemoryConfiguration config, String path) throws CorruptedValueException {
        try {
            return parse(config.getString(path));
        } catch (PatternParseException e) {
            throw new CorruptedValueException();
        }
    }

    @Override
    public Object convertToSettableType(Pattern pattern) {
        return format(pattern);
    }

    public boolean matches(String name) {
        return get().matcher(name).matches();
    }

}
