package com.monst.bankingplugin.entity;

import com.monst.bankingplugin.converter.InterestPayoutTimesConverter;
import com.monst.bankingplugin.converter.MultipliersConverter;
import com.monst.bankingplugin.converter.OfflinePlayerConverter;
import com.monst.bankingplugin.entity.geo.region.BankRegion;
import jakarta.persistence.*;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a bank.
 * A bank is a collection of {@link Account}s.
 */
@Entity
public class Bank extends AbstractEntity implements Ownable {

    @OneToMany(mappedBy = "bank")
    private Set<Account> accounts;
    @Convert(converter = OfflinePlayerConverter.class)
    private OfflinePlayer owner;
    @ElementCollection
    @CollectionTable(name = "co_owns_bank",
            joinColumns = @JoinColumn(name = "bank_id", referencedColumnName = "id")
    )
    @Column(name = "co_owner_id")
    @Convert(converter = OfflinePlayerConverter.class)
    private Set<OfflinePlayer> co_owners;
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    private BankRegion region;
    @Column(nullable = false, unique = true)
    private String name;

    private Boolean reimburseAccountCreation;
    private Boolean payOnLowBalance;
    @Column(precision = 8, scale = 4)
    private BigDecimal interestRate;
    @Column(precision = 16, scale = 2)
    private BigDecimal accountCreationPrice;
    @Column(precision = 16, scale = 2)
    private BigDecimal minimumBalance;
    @Column(precision = 16, scale = 2)
    private BigDecimal lowBalanceFee;
    private Integer allowedOfflinePayouts;
    private Integer offlineMultiplierDecrement;
    private Integer withdrawalMultiplierDecrement;
    private Integer playerBankAccountLimit;
    @Convert(converter = MultipliersConverter.class)
    private List<Integer> interestMultipliers;
    @Convert(converter = InterestPayoutTimesConverter.class)
    private Set<LocalTime> interestPayoutTimes;

    public Bank() {}

    /**
     * @param name the name of the bank
     * @param owner the owner of the bank
     * @param region the {@link BankRegion} representing the bounds of the bank
     */
    public Bank(String name, OfflinePlayer owner, BankRegion region) {

        this.generateID();
        this.name = name;
        this.owner = owner;
        this.co_owners = new HashSet<>();
        this.region = region;
        this.accounts = new HashSet<>();

    }

    public Set<Account> getAccounts() {
        return new HashSet<>(accounts);
    }

    public int getNumberOfAccounts() {
        return accounts.size();
    }

    public void addAccount(Account account) {
        if (account.getBank() != null)
            throw new IllegalStateException("Must remove account from original bank before adding it to a new one!");
        accounts.add(account);
        account.setBank(this);
    }

    public void removeAccount(Account account) {
        if (!accounts.remove(account))
            throw new IllegalStateException("Account is not located at this bank!");
        account.setBank(null);
    }

    /**
     * Calculates the sum of all {@link Account} balances at this bank.
     * @return the total value of the accounts at this bank
     * @see Account#getBalance()
     */
    public BigDecimal getTotalValue() {
        return accounts.stream().map(Account::getBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates the average of all {@link Account} balances at this bank.
     * @return the average value of the accounts at this bank
     * @see Account#getBalance()
     */
    public BigDecimal getAverageValue() {
        if (accounts.isEmpty())
            return BigDecimal.ZERO;
        return getTotalValue().divide(BigDecimal.valueOf(accounts.size()), RoundingMode.HALF_EVEN);
    }

    /**
     * @return a {@link Set<OfflinePlayer>} containing all account holders at this bank.
     */
    public Set<OfflinePlayer> getAccountHolders() {
        return accounts.stream().map(Account::getOwner).collect(Collectors.toSet());
    }

    /**
     * @return a {@link Set<OfflinePlayer>} containing all account owners
     * and account co-owners at this bank.
     */
    public Set<OfflinePlayer> getCustomers() {
        Set<OfflinePlayer> customers = new HashSet<>();
        for (Account account : accounts)
            customers.addAll(account.getTrustedPlayers());
        return customers;
    }

    public Set<CommandSender> getMailingList(CommandSender sender) {
        Set<CommandSender> recipients = new HashSet<>();
        recipients.add(sender);
        for (OfflinePlayer player : getTrustedPlayers())
            if (player.getPlayer() != null)
                recipients.add(player.getPlayer());
        for (OfflinePlayer player : getCustomers())
            if (player.getPlayer() != null)
                recipients.add(player.getPlayer());
        return recipients;
    }

    public BankRegion getRegion() {
        return region;
    }

    public void setRegion(BankRegion region) {
        this.region = region;
    }

    public Boolean getReimburseAccountCreation() {
        return reimburseAccountCreation;
    }

    public void setReimburseAccountCreation(Boolean reimburseAccountCreation) {
        this.reimburseAccountCreation = reimburseAccountCreation;
    }

    public Boolean getPayOnLowBalance() {
        return payOnLowBalance;
    }

    public void setPayOnLowBalance(Boolean payOnLowBalance) {
        this.payOnLowBalance = payOnLowBalance;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public BigDecimal getAccountCreationPrice() {
        return accountCreationPrice;
    }

    public void setAccountCreationPrice(BigDecimal accountCreationPrice) {
        this.accountCreationPrice = accountCreationPrice;
    }

    public BigDecimal getMinimumBalance() {
        return minimumBalance;
    }

    public void setMinimumBalance(BigDecimal minimumBalance) {
        this.minimumBalance = minimumBalance;
    }

    public BigDecimal getLowBalanceFee() {
        return lowBalanceFee;
    }

    public void setLowBalanceFee(BigDecimal lowBalanceFee) {
        this.lowBalanceFee = lowBalanceFee;
    }

    public Integer getAllowedOfflinePayouts() {
        return allowedOfflinePayouts;
    }

    public void setAllowedOfflinePayouts(Integer allowedOfflinePayouts) {
        this.allowedOfflinePayouts = allowedOfflinePayouts;
    }

    public Integer getOfflineMultiplierDecrement() {
        return offlineMultiplierDecrement;
    }

    public void setOfflineMultiplierDecrement(Integer offlineMultiplierDecrement) {
        this.offlineMultiplierDecrement = offlineMultiplierDecrement;
    }

    public Integer getWithdrawalMultiplierDecrement() {
        return withdrawalMultiplierDecrement;
    }

    public void setWithdrawalMultiplierDecrement(Integer withdrawalMultiplierDecrement) {
        this.withdrawalMultiplierDecrement = withdrawalMultiplierDecrement;
    }

    public Integer getPlayerBankAccountLimit() {
        return playerBankAccountLimit;
    }

    public void setPlayerBankAccountLimit(Integer playerBankAccountLimit) {
        this.playerBankAccountLimit = playerBankAccountLimit;
    }

    public List<Integer> getInterestMultipliers() {
        return interestMultipliers;
    }

    public void setInterestMultipliers(List<Integer> interestMultipliers) {
        this.interestMultipliers = interestMultipliers;
    }

    public Set<LocalTime> getInterestPayoutTimes() {
        return interestPayoutTimes;
    }

    public void setInterestPayoutTimes(Set<LocalTime> interestPayoutTimes) {
        this.interestPayoutTimes = interestPayoutTimes;
    }

    public String getName() {
        return name.replaceAll("&[0-9A-FK-OR]", "");
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColorizedName() {
        return ChatColor.translateAlternateColorCodes('&', name) + ChatColor.RESET;
    }

    @Override
    public OfflinePlayer getOwner() {
        return owner;
    }

    @Override
    public void setOwner(OfflinePlayer newOwner) {
        owner = newOwner;
        untrustPlayer(newOwner); // Remove from co-owners if new owner was a co-owner
    }

    @Override
    public Set<OfflinePlayer> getCoOwners() {
        return new HashSet<>(co_owners);
    }

    @Override
    public void trustPlayer(OfflinePlayer player) {
        if (player == null)
            return;
        getCoOwners().add(player);
    }

    @Override
    public void untrustPlayer(OfflinePlayer player) {
        if (player == null)
            return;
        getCoOwners().remove(player);
    }

    /**
     * @return whether the bank is an admin bank
     */
    public boolean isAdminBank() {
        return getOwner() == null;
    }

    /**
     * @return whether the bank is a player bank
     */
    public boolean isPlayerBank() {
        return !isAdminBank();
    }

    /**
     * Calculates Gini coefficient of this bank. This is a measurement of wealth
     * (in)equality across all accounts at the bank.
     * @return the Gini coefficient
     */
    public BigDecimal getGiniCoefficient() {
        if (getAccountHolders().size() <= 1)
            return BigDecimal.ZERO;
        BigDecimal totalValue = getTotalValue();
        if (totalValue.signum() == 0)
            return BigDecimal.ZERO;
        BigDecimal[] balances = accounts.stream()
                .collect(Collectors.toMap(Account::getOwner, Account::getBalance, BigDecimal::add))
                .values().stream().sorted().toArray(BigDecimal[]::new);
        BigDecimal topSum = BigDecimal.ZERO;
        for (int i = 1; i <= balances.length; i++) {
            BigDecimal weight = BigDecimal.valueOf((i * 2L) - balances.length - 1);
            topSum = topSum.add(balances[i - 1].multiply(weight));
        }
        BigDecimal bottomSum = totalValue.multiply(BigDecimal.valueOf(balances.length));
        return topSum.divide(bottomSum, RoundingMode.HALF_EVEN);
    }

    @Override
    public String toString() {
        return "Bank{" +
                "id=" + getID() +
                ", owner=" + owner +
                ", name='" + name + '\'' +
                '}';
    }

}
