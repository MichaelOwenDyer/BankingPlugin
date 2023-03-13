package com.monst.bankingplugin.configuration.transform;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

public class MoneyTransformer extends BigDecimalTransformer {
    
    private final BankingPlugin plugin;
    
    public MoneyTransformer(BankingPlugin plugin) {
        this.plugin = plugin;
    }
    
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();
    static {
        if (CURRENCY_FORMAT instanceof DecimalFormat)
            ((DecimalFormat) CURRENCY_FORMAT).setParseBigDecimal(true);
    }
    
    @Override
    public BigDecimal parse(String input) throws ArgumentParseException {
        if (!input.startsWith(CURRENCY_FORMAT.getCurrency().getSymbol()))
            return super.parse(input);
        try {
            Number number = CURRENCY_FORMAT.parse(input);
            if (number instanceof BigDecimal)
                return (BigDecimal) number;
            return BigDecimal.valueOf(number.doubleValue());
        } catch (ParseException e) {
            throw new ArgumentParseException(Message.NOT_A_NUMBER.with(Placeholder.INPUT).as(input));
        }
    }
    
    @Override
    public String format(BigDecimal value) {
        if (plugin.getEconomy() == null) // Will be the case at startup
            return value.toString();
        return plugin.getEconomy().format(value.doubleValue());
    }
    
}
