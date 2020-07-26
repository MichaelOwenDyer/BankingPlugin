package com.monst.bankingplugin.exceptions;

public class TransactionFailedException extends Exception {
    private static final long serialVersionUID = 3718492758430459155L;

    public TransactionFailedException(String message) {
        super("Economy transaction failed: " + message);
    }
}
