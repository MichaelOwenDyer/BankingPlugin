package com.monst.bankingplugin.exceptions;

/**
 * Thrown when a configuration value in the config.yml file is of a wrong (but similar) type, outside its bounds,
 * or otherwise invalid in a way that can be repaired. This exception should be instantiated with a replacement object
 * of the correct type to be written to the config.
 */
public class InvalidValueException extends Exception {

    private final Object replacement;

    public InvalidValueException(Object replacement) {
        this.replacement = replacement;
    }

    @SuppressWarnings("unchecked")
    public <T> T getReplacement() {
        return (T) replacement;
    }

}
