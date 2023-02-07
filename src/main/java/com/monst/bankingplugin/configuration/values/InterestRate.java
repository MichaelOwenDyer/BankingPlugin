package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.configuration.type.BigDecimalConfigurationValue;
import com.monst.bankingplugin.configuration.validation.Bound;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class InterestRate extends BigDecimalConfigurationValue implements BankPolicy<BigDecimal> {

    private static final NumberFormat PERCENT_FORMAT = NumberFormat.getPercentInstance();
    static {
        PERCENT_FORMAT.setMinimumIntegerDigits(1); // The 0 before the decimal will not disappear
        PERCENT_FORMAT.setMinimumFractionDigits(1); // 3% will display as 3.0%
        PERCENT_FORMAT.setMaximumFractionDigits(2); // 3.123% will display as 3.12%
    }

    private final AllowOverride allowOverride;

    public InterestRate(BankingPlugin plugin) {
        super(plugin, BankPolicy.defaultPath("interest-rate"), BigDecimal.ONE.scaleByPowerOfTen(-2));
        this.allowOverride = new AllowOverride(plugin, "interest-rate");
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

    @Override
    protected List<Bound<BigDecimal>> getBounds() {
        return Arrays.asList(
                Bound.requiring(b -> b.signum() > 0, BigDecimal::abs),
                Bound.requiring(b -> b.scale() == 4, b -> b.setScale(4, RoundingMode.HALF_EVEN))
        );
    }

    @Override
    public BigDecimal at(Bank bank) {
        if (bank.getInterestRate() == null) {
            if (plugin.config().stickyDefaults.get())
                bank.setInterestRate(get());
            return get();
        }
        return allowOverride.get() ? bank.getInterestRate() : get();
    }

    @Override
    public boolean parseAndSetAt(Bank bank, String input) throws ArgumentParseException {
        if (input == null || input.isEmpty()) {
            bank.setInterestRate(plugin.config().stickyDefaults.get() ? get() : null);
            return true;
        }
        bank.setInterestRate(parse(input));
        return allowOverride.get();
    }

    @Override
    public String toStringAt(Bank bank) {
        return format(Optional.ofNullable(bank.getInterestRate()).orElseGet(this));
    }
    
    @Override
    public AllowOverride getAllowOverride() {
        return allowOverride;
    }
    
}
