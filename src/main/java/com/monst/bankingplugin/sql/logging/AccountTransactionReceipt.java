package com.monst.bankingplugin.sql.logging;

import java.math.BigDecimal;
import java.util.UUID;

public class AccountTransactionReceipt extends Receipt {

    private int transactionID;
    private int accountID;
    private int bankID;
    private String executorUUID;
    private String executorName;
    private BigDecimal newBalance;
    private BigDecimal previousBalance;

    public AccountTransactionReceipt() {
        super();
    }

    public AccountTransactionReceipt(int accountID, int bankID, UUID executorUUID, String executorName, BigDecimal newBalance,
                                     BigDecimal previousBalance, BigDecimal amount, long time) {
        super(amount, time);
        this.transactionID = -1;
        this.accountID = accountID;
        this.bankID = bankID;
        this.executorUUID = executorUUID.toString();
        this.executorName = executorName;
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

}
