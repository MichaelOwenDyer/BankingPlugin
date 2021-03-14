package com.monst.bankingplugin.sql.logging;

import java.math.BigDecimal;

public class BankRevenueReceipt extends Receipt {

    private int bankID;

    public BankRevenueReceipt() {
        super();
    }

    public BankRevenueReceipt(int bankID, BigDecimal amount, long time) {
        super(amount, time);
        this.bankID = bankID;
    }

    public int getBankID() {
        return bankID;
    }

}
