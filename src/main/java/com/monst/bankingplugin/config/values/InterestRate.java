package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.exceptions.parse.DecimalParseException;
import com.monst.bankingplugin.utils.Parser;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;

public class InterestRate extends OverridableValue<Double, BigDecimal> implements NonNativeBigDecimal {

    private static final NumberFormat FORMATTER = NumberFormat.getInstance();
    static {
        FORMATTER.setMinimumIntegerDigits(1); // The 0 before the decimal will not disappear
        FORMATTER.setMinimumFractionDigits(1); // 3% will display as 3.0%
        FORMATTER.setMaximumFractionDigits(2); // 3.123% will display as 3.12%
    }

    public InterestRate(BankingPlugin plugin) {
        super(plugin, "interest-rate", BigDecimal.ONE.scaleByPowerOfTen(-2));
    }

    @Override
    public BigDecimal parse(String input) throws DecimalParseException {
        boolean percentage = input.endsWith("%");
        if (percentage)
            input = input.substring(0, input.length() - 1);
        BigDecimal bd = Parser.parseBigDecimal(input).abs();
        if (percentage)
            bd = bd.scaleByPowerOfTen(-2);
        return bd.setScale(4, RoundingMode.HALF_EVEN);
    }

    @Override
    public String format(BigDecimal value) {
        return FORMATTER.format(value.scaleByPowerOfTen(2)) + "%";
    }

}
