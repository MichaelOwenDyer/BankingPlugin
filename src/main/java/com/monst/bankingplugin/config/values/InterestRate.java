package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.exceptions.parse.DoubleParseException;
import com.monst.bankingplugin.utils.Parser;
import com.monst.bankingplugin.utils.QuickMath;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.text.NumberFormat;

public class InterestRate extends OverridableValue<Double, Double> implements NativeDouble {

    private static final NumberFormat FORMATTER = NumberFormat.getInstance();
    static {
        FORMATTER.setMinimumIntegerDigits(1); // The 0 before the decimal will not disappear
        FORMATTER.setMinimumFractionDigits(1); // 3% will display as 3.0%
        FORMATTER.setMaximumFractionDigits(2); // 3.123% will display as 3.12%
    }

    public InterestRate(BankingPlugin plugin) {
        super(plugin, "interest-rate", 0.01);
    }

    @Override
    public Double parse(@Nonnull String input) throws DoubleParseException {
        boolean percentage = input.endsWith("%");
        if (percentage)
            input = input.substring(0, input.length() - 1);
        BigDecimal bd = BigDecimal.valueOf(Parser.parseDouble(input)).abs();
        bd = QuickMath.scale(bd, 4);
        if (percentage)
            bd = QuickMath.divide(bd, 100, 4);
        return bd.doubleValue();
    }

    @Override
    public String format(Double value) {
        return FORMATTER.format(value * 100) + "%";
    }

}
