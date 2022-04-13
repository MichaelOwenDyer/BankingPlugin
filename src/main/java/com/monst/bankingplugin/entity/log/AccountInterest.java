package com.monst.bankingplugin.entity.log;

import com.monst.bankingplugin.converter.OfflinePlayerConverter;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.Bank;
import jakarta.persistence.*;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "account_interest")
public class AccountInterest extends FinancialStatement {

    @ManyToOne
    private Account account;
    @ManyToOne
    private Bank bank;
    @Column(nullable = false)
    @Convert(converter = OfflinePlayerConverter.class)
    private OfflinePlayer recipient;
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal interest;
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal lowBalanceFee;
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal finalPayment;

    public AccountInterest() {}

    public AccountInterest(Account account, Bank bank, BigDecimal interest, BigDecimal lowBalanceFee) {
        super(Instant.now());
        this.account = account;
        this.recipient = account.getOwner();
        this.bank = bank;
        this.interest = interest;
        this.lowBalanceFee = lowBalanceFee;
        this.finalPayment = interest.subtract(lowBalanceFee);
    }

    public Account getAccount() {
        return account;
    }

    public Bank getBank() {
        return bank;
    }

    public OfflinePlayer getRecipient() {
        return recipient;
    }

    public BigDecimal getInterest() {
        return interest;
    }

    public BigDecimal getLowBalanceFee() {
        return lowBalanceFee;
    }

    public BigDecimal getFinalPayment() {
        return finalPayment;
    }

    @Override
    public String toString() {
        return "AccountInterest{" +
                "account=" + account.getID() +
                ", bank=" + bank.getID() +
                ", player=" + recipient +
                ", interest=" + interest +
                ", lowBalanceFee=" + lowBalanceFee +
                ", finalPayment=" + finalPayment +
                "} " + super.toString();
    }
}
