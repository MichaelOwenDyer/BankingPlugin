package com.monst.bankingplugin.exceptions;

import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;

public abstract class ArgumentParseException extends Exception {
    private static final long serialVersionUID = 379872395581293355L;

    public ArgumentParseException() {
        super(); // TODO: Remove!
    }

    protected ArgumentParseException(Message message, String input) {
        super(LangUtils.getMessage(message, new Replacement(Placeholder.STRING, input)));
    }

}
