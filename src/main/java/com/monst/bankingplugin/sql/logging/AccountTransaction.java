package com.monst.bankingplugin.sql.logging;

import java.math.BigDecimal;
import java.util.UUID;

public class AccountTransaction extends Receipt {

    private int transactionID;
    private int accountID;
    private int bankID;
    private String executorUUID;
    private String executorName;
    private BigDecimal transactionAmount;
    private BigDecimal newBalance;
    private BigDecimal previousBalance;

    public AccountTransaction() {
        super();
    }

    public AccountTransaction(int accountID, int bankID, UUID executorUUID, String executorName,
                              BigDecimal transactionAmount, BigDecimal newBalance, BigDecimal previousBalance, long time) {
        super(time);
        this.transactionID = -1;
        this.accountID = accountID;
        this.bankID = bankID;
        this.executorUUID = executorUUID.toString();
        this.executorName = executorName;
        this.transactionAmount = transactionAmount;
        this.newBalance = newBalance;
        this.previousBalance = previousBalance;
    }

    @Override
    public int getID() {
        return transactionID;
    }

    public int getAccountID() {
        return accountID;
    }

    public int getBankID() {
        return bankID;
    }

    public String getExecutorUUID() {
        return executorUUID;
    }

    public String getExecutorName() {
        return executorName;
    }

    /**
     * Gets the amount by which the account balance changed, positive or negative.
     * @return the change in account balance.
     */
    public BigDecimal getAmount() {
        return transactionAmount;
    }

    public BigDecimal getNewBalance() {
        return newBalance;
    }

    public BigDecimal getPreviousBalance() {
        return previousBalance;
    }

}
