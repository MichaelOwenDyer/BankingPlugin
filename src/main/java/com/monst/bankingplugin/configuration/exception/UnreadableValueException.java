package com.monst.bankingplugin.configuration.exception;

/**
 * An exception that is thrown when a configuration value in the config.yml file is unreadable.
 */
public class UnreadableValueException extends Exception {
    
    public UnreadableValueException() {
    
    }
    
    public UnreadableValueException(String message) {
        super(message);
    }

}
