package com.monst.bankingplugin.exceptions;

import com.monst.bankingplugin.lang.Message;

public class LocalTimeParseException extends ArgumentParseException {

    public LocalTimeParseException(String input) {
        super(Message.NOT_A_TIME, input);
    }

}
