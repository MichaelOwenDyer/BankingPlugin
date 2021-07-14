package com.monst.bankingplugin.exceptions.parse;

import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;

public abstract class ArgumentParseException extends Exception {

    protected ArgumentParseException(Message message, String input) {
        super(LangUtils.getMessage(message, new Replacement(Placeholder.INPUT, input)));
    }

}
