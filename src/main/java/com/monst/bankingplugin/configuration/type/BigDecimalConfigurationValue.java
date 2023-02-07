package com.monst.bankingplugin.configuration.type;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;

import java.math.BigDecimal;

/**
 * A configuration value of the type {@link BigDecimal}.
 */
public abstract class BigDecimalConfigurationValue extends ConfigurationValue<BigDecimal> {

    public BigDecimalConfigurationValue(BankingPlugin plugin, String path, BigDecimal defaultValue) {
        super(plugin, path, defaultValue);
    }

    @Override
    protected BigDecimal parse(String input) throws ArgumentParseException {
        try {
            return new BigDecimal(input);
        } catch (NumberFormatException e) {
            throw new ArgumentParseException(Message.NOT_A_NUMBER.with(Placeholder.INPUT).as(input));
        }
    }
    
    /**
     * Attempts to perform a narrowing conversion to a Java Double to store in the file. If this conversion cannot be made,
     * e.g. because this BigDecimal value cannot fit inside a Double, the string representation is returned.
     * @param bd the value
     * @return a Double or String representing this value
     */
    @Override
    protected Object convertToYamlType(BigDecimal bd) {
        Double d = bd.doubleValue();
        if (d.isInfinite() || d.isNaN())
            return bd.toString(); // Store as a string if it cannot be stored as a Double
        return d;
    }

}
