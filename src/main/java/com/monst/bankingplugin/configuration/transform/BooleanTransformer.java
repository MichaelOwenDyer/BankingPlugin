package com.monst.bankingplugin.configuration.transform;

import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.configuration.exception.ValueOutOfBoundsException;
import com.monst.bankingplugin.configuration.exception.UnreadableValueException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;

public class BooleanTransformer implements Transformer<Boolean> {
    
    @Override
    public Boolean parse(String input) throws ArgumentParseException {
        if (input.equalsIgnoreCase("true")
                || input.equalsIgnoreCase("on")
                || input.equalsIgnoreCase("yes"))
            return true;
        if (input.equalsIgnoreCase("false")
                || input.equalsIgnoreCase("off")
                || input.equalsIgnoreCase("no"))
            return false;
        throw new ArgumentParseException(Message.NOT_A_BOOLEAN.with(Placeholder.INPUT).as(input));
    }
    
    @Override
    public Boolean convert(Object object) throws ValueOutOfBoundsException, UnreadableValueException {
        if (object instanceof Boolean)
            return (Boolean) object;
        if (object instanceof Integer)
            throw new ValueOutOfBoundsException((Integer) object != 0);
        return parse(String.valueOf(object));
    }
    
}
