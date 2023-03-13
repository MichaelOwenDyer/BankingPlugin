package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.configuration.transform.Transformer;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;

import java.math.BigDecimal;

/**
 * The expression used to calculate the revenue of a bank.
 */
public class BankRevenueExpression extends ConfigurationValue<BankRevenueExpression.ExpressionWrapper> {

    public BankRevenueExpression(BankingPlugin plugin) {
        super(plugin, "bank-revenue-expression",
                expressionOf("(0.10 * x) * (1 - g) * log(c)"),
                createTransformer());
    }
    
    private static Transformer<ExpressionWrapper> createTransformer() {
        return new Transformer<ExpressionWrapper>() {
            @Override
            public ExpressionWrapper parse(String input) throws ArgumentParseException {
                try {
                    ExpressionWrapper e = expressionOf(input);
                    if (e.isValidSyntax())
                        return e;
                } catch (IllegalArgumentException ignored) {}
                throw new ArgumentParseException(Message.NOT_AN_EXPRESSION.with(Placeholder.INPUT).as(input));
            }
    
            @Override
            public String format(ExpressionWrapper value) {
                return value.expressionString;
            }
    
            @Override
            public Object toYaml(ExpressionWrapper value) {
                return value.expressionString;
            }
        };
    }

    private static ExpressionWrapper expressionOf(String expressionString) {
        return new ExpressionWrapper(
                new ExpressionBuilder(expressionString)
                        .variables("x", "a", "n", "c", "g")
                        .function(new Function("ln", 1) {
                            @Override
                            public double apply(double... args) {
                                return Math.log(args[0]);
                            }
                        })
                        .build(),
                expressionString
        );
    }

    static class ExpressionWrapper {
        private final Expression expression;
        private final String expressionString;

        ExpressionWrapper(Expression expression, String expressionString) {
            this.expression = expression;
            this.expressionString = expressionString;
        }

        boolean isValidSyntax() {
            return expression.validate(false).isValid();
        }

        ExpressionWrapper withValues(double totalValue, double avgValue, int accounts, int accountHolders, double giniCoefficient) {
            expression.setVariable("x", totalValue);
            expression.setVariable("a", avgValue);
            expression.setVariable("n", accounts);
            expression.setVariable("c", accountHolders);
            expression.setVariable("g", giniCoefficient);
            return this;
        }

        BigDecimal evaluate() {
            return BigDecimal.valueOf(expression.evaluate());
        }
    }
    
    public BigDecimal evaluate(double totalValue, double avgValue, int accounts, int accountHolders, double giniCoefficient) {
        try {
            return get().withValues(totalValue, avgValue, accounts, accountHolders, giniCoefficient).evaluate();
        } catch (IllegalArgumentException e) {
            return BigDecimal.ZERO;
        }
    }

}
