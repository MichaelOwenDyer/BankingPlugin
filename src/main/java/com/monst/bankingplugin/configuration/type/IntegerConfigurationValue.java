package com.monst.bankingplugin.configuration.type;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.configuration.exception.UnreadableValueException;
import com.monst.bankingplugin.configuration.exception.ValueOutOfBoundsException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;

/**
 * A configuration value of the type {@link Integer}.
 */
public abstract class IntegerConfigurationValue extends ConfigurationValue<Integer> {

    public IntegerConfigurationValue(BankingPlugin plugin, String path, Integer defaultValue) {
        super(plugin, path, defaultValue);
    }
    
    @Override
    protected Integer parse(String input) throws ArgumentParseException {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new ArgumentParseException(Message.NOT_AN_INTEGER.with(Placeholder.INPUT).as(input));
        }
    }
    
    @Override
    protected Integer convert(Object o)
            throws ValueOutOfBoundsException, UnreadableValueException {
        if (o instanceof Integer)
            return (Integer) o;
        if (o instanceof Double)
            throw new ValueOutOfBoundsException(((Double) o).intValue());
        try {
            throw new ValueOutOfBoundsException(Integer.parseInt(o.toString()));
        } catch (NumberFormatException e) {
            throw new UnreadableValueException();
        }
    }

}
