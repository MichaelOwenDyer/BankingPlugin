package com.monst.bankingplugin.configuration.exception;

import com.monst.bankingplugin.lang.Translatable;

/**
 * An exception that is thrown when a user-inputted string could not be parsed.
 */
public class ArgumentParseException extends Exception {
    
    private final Translatable translatable;
    
    public ArgumentParseException(Translatable translatable) {
        this.translatable = translatable;
    }
    
    public Translatable getTranslatableMessage() {
        return translatable;
    }
    
    @Override
    public String getMessage() {
        return translatable.inEnglish();
    }
    
}
