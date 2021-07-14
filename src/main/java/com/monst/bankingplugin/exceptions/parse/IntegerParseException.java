package com.monst.bankingplugin.exceptions.parse;

import com.monst.bankingplugin.lang.Message;

public class IntegerParseException extends ArgumentParseException {

    public IntegerParseException(String input) {
        super(Message.NOT_AN_INTEGER, input);
    }

}
