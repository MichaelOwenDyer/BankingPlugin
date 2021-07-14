package com.monst.bankingplugin.exceptions;

public class CorruptedValueException extends Exception {

    private final Object replacement;

    public CorruptedValueException() {
        super();
        replacement = null;
    }

    public CorruptedValueException(Object replacement) {
        this.replacement = replacement;
    }

    @SuppressWarnings("unchecked")
    public <T> T getReplacement() {
        return (T) replacement;
    }

    public boolean hasReplacement() {
        return replacement != null;
    }

}
