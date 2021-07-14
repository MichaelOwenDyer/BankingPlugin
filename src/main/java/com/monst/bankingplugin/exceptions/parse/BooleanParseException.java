package com.monst.bankingplugin.exceptions.parse;

import com.monst.bankingplugin.lang.Message;

public class BooleanParseException extends ArgumentParseException {

    public BooleanParseException(String input) {
        super(Message.NOT_A_BOOLEAN, input);
    }

}
