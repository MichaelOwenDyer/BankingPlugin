package com.monst.bankingplugin.exceptions.parse;

import com.monst.bankingplugin.lang.Message;

public class PatternParseException extends ArgumentParseException {

    public PatternParseException(String input) {
        super(Message.NOT_A_PATTERN, input);
    }

}
