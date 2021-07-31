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
        FORMATTER.setMinimumIntegerDigits(1);
        FORMATTER.setMinimumFractionDigits(1);
        FORMATTER.setMaximumFractionDigits(2);
    }

    public InterestRate(BankingPlugin plugin) {
        super(plugin, "interest-rate", 0.01);
    }

    @Override
    public Double parse(@Nonnull String input) throws DoubleParseException {
        BigDecimal bd = QuickMath.scale(BigDecimal.valueOf(Parser.parseDouble(input)).abs(), 4);
        if (input.endsWith("%"))
            bd = QuickMath.divide(bd, 100);
        return bd.doubleValue();
    }

    @Override
    public String format(Double value) {
        return FORMATTER.format(value * 100) + "%";
    }

}
