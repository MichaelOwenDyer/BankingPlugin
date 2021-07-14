package com.monst.bankingplugin.exceptions;

public class TransactionFailedException extends Exception {

    public TransactionFailedException(String message) {
        super("Economy transaction failed: " + message);
    }

}
