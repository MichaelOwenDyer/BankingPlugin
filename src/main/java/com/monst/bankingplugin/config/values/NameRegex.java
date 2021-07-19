package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.CorruptedValueException;
import com.monst.bankingplugin.exceptions.parse.PatternParseException;
import com.monst.bankingplugin.utils.Parser;

import java.util.regex.Pattern;

public class NameRegex extends ConfigValue<String, Pattern> implements NonNativeValue<String, Pattern> {

    public NameRegex() {
        super("name-regex", Pattern.compile(".*"));
    }

    @Override
    public Pattern parse(String input) throws PatternParseException {
        return Parser.parsePattern(input);
    }

    @Override
    public String cast(Object o) {
        return (String) o;
    }

    @Override
    public Pattern translate(String s) throws CorruptedValueException {
        try {
            return parse(s);
        } catch (PatternParseException e) {
            throw new CorruptedValueException();
        }
    }

    public boolean matches(String name) {
        return get().matcher(name).matches();
    }

}
