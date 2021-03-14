package com.monst.bankingplugin.sql.logging;

import java.math.BigDecimal;
import java.util.UUID;

public class AccountTransactionReceipt extends Receipt {

    private int accountID;
    private int bankID;
    private UUID executorUUID;
    private String executorName;
    private BigDecimal newBalance;
    private BigDecimal previousBalance;

    public AccountTransactionReceipt() {
        super();
    }

    public AccountTransactionReceipt(int accountID, int bankID, UUID executorUUID, String executorName, BigDecimal newBalance,
                                     BigDecimal previousBalance, BigDecimal amount, long time) {
        super(amount, time);
        this.accountID = accountID;
        this.bankID = bankID;
        this.executorUUID = executorUUID;
        this.executorName = executorName;
        this.newBalance = newBalance;
        this.previousBalance = previousBalance;
    }

    public int getAccountID() {
        return accountID;
    }

    public int getBankID() {
        return bankID;
    }

    public UUID getExecutorUUID() {
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
