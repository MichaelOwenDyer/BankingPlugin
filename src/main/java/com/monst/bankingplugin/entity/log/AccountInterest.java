package com.monst.bankingplugin.entity.log;

import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.Bank;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.time.Instant;

public class AccountInterest extends FinancialStatement {

    private final int accountID;
    private final int bankID;
    private final OfflinePlayer recipient;
    private final BigDecimal interest;
    private final BigDecimal lowBalanceFee;
    private final BigDecimal finalPayment;

    public AccountInterest(int id, Instant timestamp, int accountID, int bankID, OfflinePlayer recipient, BigDecimal interest, BigDecimal lowBalanceFee) {
        super(id, timestamp);
        this.accountID = accountID;
        this.bankID = bankID;
        this.recipient = recipient;
        this.interest = interest;
        this.lowBalanceFee = lowBalanceFee;
        this.finalPayment = interest.subtract(lowBalanceFee);
    }

    public AccountInterest(Account account, Bank bank, BigDecimal interest, BigDecimal lowBalanceFee) {
        super(Instant.now());
        this.accountID = account.getID();
        this.bankID = bank.getID();
        this.recipient = account.getOwner();
        this.interest = interest;
        this.lowBalanceFee = lowBalanceFee;
        this.finalPayment = interest.subtract(lowBalanceFee);
    }

    public int getAccountID() {
        return accountID;
    }

    public int getBankID() {
        return bankID;
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
                "account=" + accountID +
                ", bank=" + bankID +
                ", player=" + recipient +
                ", interest=" + interest +
                ", lowBalanceFee=" + lowBalanceFee +
                ", finalPayment=" + finalPayment +
                "} " + super.toString();
    }
}
