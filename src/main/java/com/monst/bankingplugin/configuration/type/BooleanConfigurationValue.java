package com.monst.bankingplugin.configuration.type;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.configuration.exception.UnreadableValueException;
import com.monst.bankingplugin.configuration.exception.ValueOutOfBoundsException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;

/**
 * A configuration value of the type {@link Boolean}.
 * This class avoids the use of {@link Boolean#parseBoolean(String)} in favor of a more strict parser.
 */
public abstract class BooleanConfigurationValue extends ConfigurationValue<Boolean> {

    public BooleanConfigurationValue(BankingPlugin plugin, String path, Boolean defaultValue) {
        super(plugin, path, defaultValue);
    }

    @Override
    protected Boolean parse(String input) throws ArgumentParseException {
        if (input.equalsIgnoreCase("true"))
            return true;
        if (input.equalsIgnoreCase("false"))
            return false;
        throw new ArgumentParseException(Message.NOT_A_BOOLEAN.with(Placeholder.INPUT).as(input));
    }

    @Override
    protected Boolean convert(Object o) throws ValueOutOfBoundsException, UnreadableValueException {
        if (o instanceof Boolean)
            return (Boolean) o;
        try {
            throw new ValueOutOfBoundsException(parse(o.toString()));
        } catch (ArgumentParseException e) {
            throw new UnreadableValueException();
        }
    }

}
