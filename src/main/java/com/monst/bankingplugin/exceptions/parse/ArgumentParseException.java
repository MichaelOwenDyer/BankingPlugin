package com.monst.bankingplugin.exceptions.parse;

import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;

public abstract class ArgumentParseException extends Exception {

    protected ArgumentParseException(Message message, String input) {
        super(message.with(Placeholder.INPUT).as(input).translate());
    }

}
