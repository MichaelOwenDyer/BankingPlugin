package com.monst.bankingplugin.configuration.transform;

import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;

public class PercentageTransformer extends BigDecimalTransformer {
    
    private static final NumberFormat PERCENT_FORMAT = NumberFormat.getPercentInstance();
    static {
        PERCENT_FORMAT.setMinimumIntegerDigits(1); // The 0 before the decimal will not disappear
        PERCENT_FORMAT.setMinimumFractionDigits(1); // 3% will display as 3.0%
        PERCENT_FORMAT.setMaximumFractionDigits(2); // 3.123% will display as 3.12%
    }
    
    @Override
    public BigDecimal parse(String input) throws ArgumentParseException {
        if (!input.endsWith("%"))
            return super.parse(input);
        try {
            return BigDecimal.valueOf(PERCENT_FORMAT.parse(input).doubleValue());
        } catch (ParseException e) {
            throw new ArgumentParseException(Message.NOT_A_NUMBER.with(Placeholder.INPUT).as(input));
        }
    }
    
    @Override
    public String format(BigDecimal value) {
        return PERCENT_FORMAT.format(value);
    }
    
}
