package com.monst.bankingplugin.exceptions.parse;

import com.monst.bankingplugin.lang.Message;

public class ExpressionParseException extends ArgumentParseException {

    public ExpressionParseException(String input) {
        super(Message.NOT_A_FUNCTION, input);
    }

}
