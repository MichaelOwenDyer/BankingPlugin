package com.monst.bankingplugin.sql.logging;

import java.math.BigDecimal;

public class AccountInterestReceipt extends Receipt {

    private int accountID;
    private int bankID;

    public AccountInterestReceipt() {
        super();
    }

    public AccountInterestReceipt(int accountID, int bankID, BigDecimal amount, long time) {
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
