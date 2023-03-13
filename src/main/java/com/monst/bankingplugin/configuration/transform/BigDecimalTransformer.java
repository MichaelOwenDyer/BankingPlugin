package com.monst.bankingplugin.configuration.transform;

import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.configuration.validation.Bound;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalTransformer implements Transformer<BigDecimal> {
    
    @Override
    public BigDecimal parse(String input) throws ArgumentParseException {
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
    public Object toYaml(BigDecimal bd) {
        Double d = bd.doubleValue();
        if (d.isInfinite() || d.isNaN())
            return bd.toString(); // Store as a string if it cannot be stored as a Double
        return d;
    }
    
    public static Bound<BigDecimal> positive() {
        return Bound.requiring(b -> b.signum() >= 0, BigDecimal::abs);
    }
    
    public static Bound<BigDecimal> scale(int scale) {
        return Bound.requiring(b -> b.scale() == scale, b -> b.setScale(scale, RoundingMode.HALF_EVEN));
    }
    
}
