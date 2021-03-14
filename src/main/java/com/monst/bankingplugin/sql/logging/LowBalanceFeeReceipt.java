package com.monst.bankingplugin.sql.logging;

import java.math.BigDecimal;

public class LowBalanceFeeReceipt extends Receipt {

    private int accountID;
    private int bankID;

    public LowBalanceFeeReceipt() {
        super();
    }

    public LowBalanceFeeReceipt(int accountID, int bankID, BigDecimal amount, long time) {
        super(amount, time);
        this.accountID = accountID;
        this.bankID = bankID;
    }

    public int getAccountID() {
        return accountID;
    }

    public int getBankID() {
        return bankID;
    }

}
