package com.monst.bankingplugin.configuration.transform;

import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PatternTransformer implements Transformer<Pattern> {
    
    @Override
    public Pattern parse(String input) throws ArgumentParseException {
        try {
            return Pattern.compile(input);
        } catch (PatternSyntaxException e) {
            throw new ArgumentParseException(Message.NOT_A_REGULAR_EXPRESSION.with(Placeholder.INPUT).as(input));
        }
    }
    
}
