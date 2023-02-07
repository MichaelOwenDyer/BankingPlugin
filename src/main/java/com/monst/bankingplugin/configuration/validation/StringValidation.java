package com.monst.bankingplugin.configuration.validation;

public interface StringValidation {

    static Bound<String> lowercase() {
        return Bound.requiring(s -> s.chars().noneMatch(Character::isUpperCase), String::toLowerCase);
    }

    static Bound<String> noSpaces() {
        return Bound.requiring(s -> s.chars().noneMatch(Character::isSpaceChar), s -> s.replaceAll("\\s", ""));
    }

}
