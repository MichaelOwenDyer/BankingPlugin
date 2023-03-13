package com.monst.bankingplugin.configuration.transform;

import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.configuration.exception.ValueOutOfBoundsException;
import com.monst.bankingplugin.configuration.exception.UnreadableValueException;
import com.monst.bankingplugin.configuration.validation.Bound;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;

public class IntegerTransformer implements Transformer<Integer> {
    
    @Override
    public Integer parse(String input) throws ArgumentParseException {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new ArgumentParseException(Message.NOT_AN_INTEGER.with(Placeholder.INPUT).as(input));
        }
    }
    
    @Override
    public Integer convert(Object object) throws ValueOutOfBoundsException, UnreadableValueException {
        if (object instanceof Integer)
            return (Integer) object;
        if (object instanceof Number)
            throw new ValueOutOfBoundsException(((Number) object).intValue());
        return parse(object.toString());
    }
    
    public Transformer<Integer> absolute() {
        return bounded(Bound.requiring(i -> i >= 0, Math::abs));
    }
    
    public Transformer<Integer> atLeast(int min) {
        return bounded(Bound.atLeast(min));
    }
    
}
