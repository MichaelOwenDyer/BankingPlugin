package com.monst.bankingplugin.configuration.transform;

import com.monst.bankingplugin.configuration.validation.Bound;

public class StringTransformer implements Transformer<String> {
    
    @Override
    public String parse(String input) {
        return input;
    }
    
    @Override
    public String format(String value) {
        return value;
    }
    
    public static Bound<String> lowercase() {
        return Bound.requiring(s -> s.chars().noneMatch(Character::isUpperCase), String::toLowerCase);
    }
    
    public static Bound<String> noSpaces() {
        return Bound.requiring(s -> s.chars().noneMatch(Character::isSpaceChar), s -> s.replaceAll("\\s", ""));
    }
    
}
