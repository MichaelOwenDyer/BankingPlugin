package com.monst.bankingplugin.exceptions;

import com.monst.bankingplugin.lang.Message;

public class ListParseException extends ArgumentParseException {

    public ListParseException(String input) {
        super(Message.NOT_A_LIST, input);
    }

}
