package com.monst.bankingplugin.config.values.simple;

public class BankRevenueFunction extends SimpleString {

    public BankRevenueFunction() {
        super("bank-revenue-function", "(0.10 * x) * (1 - g) * ln(n)");
    }

}
