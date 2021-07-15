package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.config.values.ConfigValue;
import com.monst.bankingplugin.exceptions.CorruptedValueException;
import com.monst.bankingplugin.exceptions.parse.ExpressionParseException;
import com.monst.bankingplugin.utils.QuickMath;
import org.bukkit.configuration.MemoryConfiguration;
import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;

import java.math.BigDecimal;

public class BankRevenueFunction extends ConfigValue<Expression> {

    private static final Argument[] ARGS = new Argument[] {
            new Argument("x"), // Total value of bank
            new Argument("a"), // Average account value
            new Argument("n"), // Total number of accounts
            new Argument("c"), // Total number of account holders
            new Argument("g"), // Gini coefficient
    };

    public BankRevenueFunction() {
        super("bank-revenue-function", new Expression("(0.10 * x) * (1 - g) * ln(c)", ARGS));
    }

    @Override
    public Expression parse(String input) throws ExpressionParseException {
        Expression expression = new Expression(input, ARGS);
        if (!expression.checkSyntax())
            throw new ExpressionParseException(input);
        return expression;
    }

    @Override
    public Expression readFromFile(MemoryConfiguration config, String path) throws CorruptedValueException {
        try {
            return parse(config.getString(path));
        } catch (ExpressionParseException e) {
            throw new CorruptedValueException();
        }
    }

    @Override
    public String format(Expression expression) {
        return expression.getExpressionString();
    }

    @Override
    public Object convertToSettableType(Expression expression) {
        return expression.getExpressionString();
    }

    public BigDecimal evaluate(double totalValue, double avgValue, int accounts, int accountHolders, double giniCoefficient) {
        Expression expression = get();
        expression.setArgumentValue("x", totalValue);
        expression.setArgumentValue("a", avgValue);
        expression.setArgumentValue("n", accounts);
        expression.setArgumentValue("c", accountHolders);
        expression.setArgumentValue("g", giniCoefficient);
        double result = expression.calculate();
        if (Double.isInfinite(result) || Double.isNaN(result))
            return BigDecimal.ZERO;
        return QuickMath.scale(BigDecimal.valueOf(result));
    }

}
