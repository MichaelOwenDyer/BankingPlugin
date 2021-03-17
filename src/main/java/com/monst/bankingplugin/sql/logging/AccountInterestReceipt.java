package com.monst.bankingplugin.sql.logging;

import java.math.BigDecimal;

public class AccountInterestReceipt extends Receipt {

    private int interestID;
    private int accountID;
    private int bankID;
    private BigDecimal lowBalanceFee;

    public AccountInterestReceipt() {
        super();
    }

    public AccountInterestReceipt(int accountID, int bankID, BigDecimal amount, BigDecimal lowBalanceFee, long time) {
        super(amount, time);
        this.interestID = -1;
        this.accountID = accountID;
        this.bankID = bankID;
        this.lowBalanceFee = lowBalanceFee;
    }

    @Override
    public int getID() {
        return interestID;
    }

    public int getAccountID() {
        return accountID;
    }

    public int getBankID() {
        return bankID;
    }

    public BigDecimal getLowBalanceFee() {
        return lowBalanceFee;
    }
}
