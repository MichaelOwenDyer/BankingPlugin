package com.monst.bankingplugin.sql.logging;

import java.math.BigDecimal;

public class BankRevenueReceipt extends Receipt {

    private int revenueID;
    private int bankID;

    public BankRevenueReceipt() {
        super();
    }

    public BankRevenueReceipt(int bankID, BigDecimal amount, long time) {
        super(amount, time);
        this.revenueID = -1;
        this.bankID = bankID;
    }

    @Override
    public int getID() {
        return revenueID;
    }

    public int getBankID() {
        return bankID;
    }

}
