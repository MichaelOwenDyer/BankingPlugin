package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.parse.PatternParseException;
import com.monst.bankingplugin.utils.Parser;

import java.util.regex.Pattern;

public class NameRegex extends ConfigValue<String, Pattern> implements NonNativeString<Pattern> {

    public NameRegex() {
        super("name-regex", Pattern.compile(".*"));
    }

    @Override
    public Pattern parse(String input) throws PatternParseException {
        return Parser.parsePattern(input);
    }

    public boolean matches(String name) {
        return get().matcher(name).matches();
    }

}
