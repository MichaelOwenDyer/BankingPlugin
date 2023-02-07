package com.monst.bankingplugin.entity.log;

import com.monst.bankingplugin.entity.Bank;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.time.Instant;

public class BankIncome extends FinancialStatement {

    private final int bankID;
    private final OfflinePlayer recipient;
    private final BigDecimal revenue;
    private final BigDecimal interest;
    private final BigDecimal lowBalanceFees;
    private final BigDecimal netIncome;

    public BankIncome(int id, Instant timestamp, int bankID, OfflinePlayer recipient,
                      BigDecimal revenue, BigDecimal interest, BigDecimal lowBalanceFees, BigDecimal netIncome) {
        super(id, timestamp);
        this.bankID = bankID;
        this.recipient = recipient;
        this.revenue = revenue;
        this.interest = interest;
        this.lowBalanceFees = lowBalanceFees;
        this.netIncome = netIncome;
    }

    public BankIncome(Bank bank, BigDecimal revenue, BigDecimal interest, BigDecimal lowBalanceFees) {
        super(Instant.now());
        this.bankID = bank.getID();
        this.recipient = bank.getOwner();
        this.revenue = revenue;
        this.interest = interest;
        this.lowBalanceFees = lowBalanceFees;
        this.netIncome = revenue.add(lowBalanceFees).subtract(interest);
    }

    public int getBankID() {
        return bankID;
    }

    public OfflinePlayer getRecipient() {
        return recipient;
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
     * @return the net income of the bank owner
     */
    public BigDecimal getNetIncome() {
        return netIncome;
    }

    @Override
    public String toString() {
        return "BankIncome{" +
                "bank=" + bankID +
                ", owner=" + recipient +
                ", revenue=" + revenue +
                ", interest=" + interest +
                ", lowBalanceFees=" + lowBalanceFees +
                ", netIncome=" + getNetIncome() +
                "} " + super.toString();
    }
}
