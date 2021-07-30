package com.monst.bankingplugin.exceptions.parse;

import com.monst.bankingplugin.lang.Message;

public class PathParseException extends ArgumentParseException {

    public PathParseException(String input) {
        super(Message.NOT_A_FILENAME, input);
    }

}
