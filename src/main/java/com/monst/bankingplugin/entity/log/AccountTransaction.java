package com.monst.bankingplugin.entity.log;

import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.Bank;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.time.Instant;

public class AccountTransaction extends FinancialStatement {

    private final int accountID;
    private final int bankID;
    private final OfflinePlayer executor;
    private final BigDecimal previousBalance;
    private final BigDecimal amount;
    private final BigDecimal newBalance;

    public AccountTransaction(int id, Instant timestamp, int accountID, int bankID, OfflinePlayer executor,
                              BigDecimal previousBalance, BigDecimal amount, BigDecimal newBalance) {
        super(id, timestamp);
        this.accountID = accountID;
        this.bankID = bankID;
        this.executor = executor;
        this.previousBalance = previousBalance;
        this.amount = amount;
        this.newBalance = newBalance;
    }

    public AccountTransaction(Account account, Bank bank, OfflinePlayer executor,
                              BigDecimal previousBalance, BigDecimal amount, BigDecimal newBalance) {
        super(Instant.now());
        this.accountID = account.getID();
        this.bankID = bank.getID();
        this.executor = executor;
        this.previousBalance = previousBalance;
        this.amount = amount;
        this.newBalance = newBalance;
    }

    public int getAccountID() {
        return accountID;
    }

    public int getBankID() {
        return bankID;
    }

    public OfflinePlayer getExecutor() {
        return executor;
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

    @Override
    public String toString() {
        return "AccountTransaction{" +
                "account=" + accountID +
                ", bank=" + bankID +
                ", player=" + executor +
                ", newBalance=" + newBalance +
                ", previousBalance=" + previousBalance +
                ", difference=" + amount +
                "} " + super.toString();
    }
}
