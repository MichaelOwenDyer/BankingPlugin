package com.monst.bankingplugin.exceptions;

public class ChestNotFoundException extends Exception {
    private static final long serialVersionUID = -6446875473671870708L;

    public ChestNotFoundException(String message) {
        super(message);
    }
}
