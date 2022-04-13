package com.monst.bankingplugin.entity.log;

import com.monst.bankingplugin.converter.OfflinePlayerConverter;
import com.monst.bankingplugin.entity.Bank;
import jakarta.persistence.*;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "bank_income")
public class BankIncome extends FinancialStatement {

    @ManyToOne
    private Bank bank;
    @Convert(converter = OfflinePlayerConverter.class)
    private OfflinePlayer recipient;
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal revenue;
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal interest;
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal lowBalanceFees;
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal netIncome;

    public BankIncome() {}

    public BankIncome(Bank bank, BigDecimal revenue, BigDecimal interest, BigDecimal lowBalanceFees) {
        super(Instant.now());
        this.bank = bank;
        this.recipient = bank.getOwner();
        this.revenue = revenue;
        this.interest = interest;
        this.lowBalanceFees = lowBalanceFees;
        this.netIncome = revenue.add(lowBalanceFees).subtract(interest);
    }

    public Bank getBank() {
        return bank;
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
                "bank=" + bank.getID() +
                ", owner=" + recipient +
                ", revenue=" + revenue +
                ", interest=" + interest +
                ", lowBalanceFees=" + lowBalanceFees +
                ", netIncome=" + getNetIncome() +
                "} " + super.toString();
    }
}
