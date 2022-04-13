package com.monst.bankingplugin.entity.log;

import com.monst.bankingplugin.converter.OfflinePlayerConverter;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.Bank;
import jakarta.persistence.*;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "account_transaction")
public class AccountTransaction extends FinancialStatement {

    @ManyToOne
    private Account account;
    @ManyToOne
    private Bank bank;
    @Column(nullable = false)
    @Convert(converter = OfflinePlayerConverter.class)
    private OfflinePlayer executor;
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal newBalance;
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal previousBalance;
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal difference;

    public AccountTransaction() {
    }

    public AccountTransaction(Account account, Bank bank, OfflinePlayer executor, BigDecimal newBalance, BigDecimal previousBalance, BigDecimal difference) {
        super(Instant.now());
        this.account = account;
        this.bank = bank;
        this.executor = executor;
        this.newBalance = newBalance;
        this.previousBalance = previousBalance;
        this.difference = difference;
    }

    public Account getAccount() {
        return account;
    }

    public Bank getBank() {
        return bank;
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
    public BigDecimal getDifference() {
        return difference;
    }

    @Override
    public String toString() {
        return "AccountTransaction{" +
                "account=" + account.getID() +
                ", bank=" + bank.getID() +
                ", player=" + executor +
                ", newBalance=" + newBalance +
                ", previousBalance=" + previousBalance +
                ", difference=" + difference +
                "} " + super.toString();
    }
}
