package com.monst.bankingplugin.exceptions;

import com.monst.bankingplugin.utils.Messages;

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

    public String getErrorMessage() {
        if (dataType.equals(Double.class))
            return String.format(Messages.NOT_A_NUMBER, value);
        if (dataType.equals(Integer.class))
            return String.format(Messages.NOT_AN_INTEGER, value);
        if (dataType.equals(List.class))
            return String.format(Messages.NOT_A_LIST, value);
        return "";
    }

}
