package com.monst.bankingplugin.exceptions;

import com.monst.bankingplugin.lang.LangUtils;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.lang.Replacement;

import java.util.List;

public class ArgumentParseException extends Exception {
    private static final long serialVersionUID = 379872395581293355L;

    private final String value;
    private final Class<?> dataType;

    public ArgumentParseException(Class<?> dataType, String value) {
        super(value);
        this.value = value;
        this.dataType = dataType;
    }

    @Override
    public String getLocalizedMessage() {
        if (dataType.equals(Double.class))
            return LangUtils.getMessage(Message.NOT_A_NUMBER, new Replacement(Placeholder.STRING, value));
        if (dataType.equals(Integer.class))
            return LangUtils.getMessage(Message.NOT_AN_INTEGER, new Replacement(Placeholder.STRING, value));
        if (dataType.equals(List.class))
            return LangUtils.getMessage(Message.NOT_A_LIST, new Replacement(Placeholder.STRING, value));
        return LangUtils.getMessage(Message.ERROR_OCCURRED, new Replacement(Placeholder.ERROR, ""));
    }
}
