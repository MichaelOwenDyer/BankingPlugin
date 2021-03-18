package com.monst.bankingplugin.sql.logging;

import java.math.BigDecimal;

public class BankProfit extends Receipt {

    private int revenueID;
    private int bankID;
    private BigDecimal revenue;
    private BigDecimal interest;
    private BigDecimal lowBalanceFees;

    public BankProfit() {
        super();
    }

    public BankProfit(int bankID, BigDecimal revenue, BigDecimal interest, BigDecimal lowBalanceFees, long time) {
        super(time);
        this.revenueID = -1;
        this.bankID = bankID;
        this.revenue = revenue;
        this.interest = interest;
        this.lowBalanceFees = lowBalanceFees;
    }

    @Override
    public int getID() {
        return revenueID;
    }

    public int getBankID() {
        return bankID;
    }

    /**
     * Gets the revenue earned on the bank following the config bank revenue function.
     * @return the total revenue the bank owner earns
     */
    public BigDecimal getRevenue() {
        return revenue;
    }

    /**
     * Gets the amount of interest the bank owner must pay their account holders.
     * @return the total interest the bank owner must pay
     */
    public BigDecimal getInterest() {
        return interest;
    }

    /**
     * Gets the amount of low balance fees the bank owner receives from their account holders.
     * @return the total low balance fees the bank owner receives
     */
    public BigDecimal getLowBalanceFees() {
        return lowBalanceFees;
    }

    /**
     * Gets the amount of money the bank owner actually earned or lost after paying account holders
     * their interest and receiving low balance fees.
     * @return the profit earned by the bank owner
     */
    public BigDecimal getProfit() {
        return getRevenue().subtract(getInterest()).add(getLowBalanceFees());
    }

}
