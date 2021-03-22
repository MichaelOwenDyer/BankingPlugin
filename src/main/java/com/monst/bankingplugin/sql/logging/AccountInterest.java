package com.monst.bankingplugin.sql.logging;

import java.math.BigDecimal;

public class AccountInterest extends Receipt {

    private int interestID;
    private int accountID;
    private int bankID;
    private BigDecimal interest;
    private BigDecimal lowBalanceFee;
    private BigDecimal finalPayment;

    public AccountInterest() {
        super();
    }

    public AccountInterest(int accountID, int bankID, BigDecimal interest, BigDecimal lowBalanceFee, long time) {
        super(time);
        this.interestID = -1;
        this.accountID = accountID;
        this.bankID = bankID;
        this.interest = interest;
        this.lowBalanceFee = lowBalanceFee;
        this.finalPayment = interest.subtract(lowBalanceFee);
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

    public BigDecimal getInterest() {
        return interest;
    }

    public BigDecimal getLowBalanceFee() {
        return lowBalanceFee;
    }

    /**
     * Gets the amount of money the account holder actually receives (or must pay!)
     * after the low balance fee is applied.
     * @return the final value of this interest payment
     */
    public BigDecimal getFinalPayment() {
        return finalPayment;
    }

}
