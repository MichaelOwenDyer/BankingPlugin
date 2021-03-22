package com.monst.bankingplugin.sql.logging;

import java.math.BigDecimal;
import java.util.UUID;

public class AccountTransaction extends Receipt {

    private int transactionID;
    private int accountID;
    private int bankID;
    private String executorUUID;
    private String executorName;
    private BigDecimal newBalance;
    private BigDecimal previousBalance;
    private BigDecimal amount;

    public AccountTransaction() {
        super();
    }

    public AccountTransaction(int accountID, int bankID, UUID executorUUID, String executorName,
                              BigDecimal amount, BigDecimal newBalance, BigDecimal previousBalance, long time) {
        super(time);
        this.transactionID = -1;
        this.accountID = accountID;
        this.bankID = bankID;
        this.executorUUID = executorUUID.toString();
        this.executorName = executorName;
        this.amount = amount;
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

    public BigDecimal getNewBalance() {
        return newBalance;
    }

    public BigDecimal getPreviousBalance() {
        return previousBalance;
    }

    /**
     * Gets the amount by which the account balance changed, positive or negative.
     * @return the change in account balance.
     */
    public BigDecimal getAmount() {
        return amount;
    }

}
