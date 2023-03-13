package com.monst.bankingplugin.configuration.transform;

import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.configuration.exception.ValueOutOfBoundsException;
import com.monst.bankingplugin.configuration.exception.UnreadableValueException;
import com.monst.bankingplugin.configuration.validation.Bound;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;

public class LongTransformer implements Transformer<Long> {
    
    @Override
    public Long parse(String input) throws ArgumentParseException {
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) {
            throw new ArgumentParseException(Message.NOT_AN_INTEGER.with(Placeholder.INPUT).as(input));
        }
    }
    
    @Override
    public Long convert(Object object) throws ValueOutOfBoundsException, UnreadableValueException {
        if (object instanceof Long)
            return (Long) object;
        if (object instanceof Integer)
            return ((Integer) object).longValue();
        if (object instanceof Number)
            throw new ValueOutOfBoundsException(((Number) object).intValue());
        return parse(object.toString());
    }
    
    public Transformer<Long> absolute() {
        return bounded(Bound.requiring(i -> i >= 0, Math::abs));
    }
    
}
