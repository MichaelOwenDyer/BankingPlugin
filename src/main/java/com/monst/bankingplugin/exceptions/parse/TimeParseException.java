package com.monst.bankingplugin.exceptions.parse;

import com.monst.bankingplugin.lang.Message;

public class TimeParseException extends ArgumentParseException {

    public TimeParseException(String input) {
        super(Message.NOT_A_TIME, input);
    }

}
