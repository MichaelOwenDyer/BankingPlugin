package com.monst.bankingplugin.exceptions.parse;

import com.monst.bankingplugin.lang.Message;

public class DecimalParseException extends ArgumentParseException {

    public DecimalParseException(String input) {
        super(Message.NOT_A_NUMBER, input);
    }

}
