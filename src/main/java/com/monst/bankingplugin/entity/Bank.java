package com.monst.bankingplugin.entity;

import com.monst.bankingplugin.entity.geo.region.BankRegion;
import com.monst.bankingplugin.util.Observable;
import com.monst.bankingplugin.util.Observer;
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
public class Bank extends Entity implements Ownable, Observable {
    
    private enum Policy {
        INTEREST_RATE,
        ACCOUNT_CREATION_PRICE,
        MINIMUM_BALANCE,
        LOW_BALANCE_FEE,
        ALLOWED_OFFLINE_PAYOUTS,
        OFFLINE_MULTIPLIER_DECREMENT,
        WITHDRAWAL_MULTIPLIER_DECREMENT,
        PLAYER_BANK_ACCOUNT_LIMIT,
        REIMBURSE_ACCOUNT_CREATION,
        PAY_ON_LOW_BALANCE,
        INTEREST_MULTIPLIERS,
        INTEREST_PAYOUT_TIMES
    }
    
    private String name;
    private OfflinePlayer owner;
    private BankRegion region;
    private final Set<OfflinePlayer> co_owners = new HashSet<>();
    private final Set<Account> accounts = new HashSet<>();
    private final Map<Policy, Object> policies = new EnumMap<>(Policy.class);
    
    public Bank(int id, String name, OfflinePlayer owner, BankRegion region, Set<OfflinePlayer> coOwners,
                BigDecimal interestRate, BigDecimal accountCreationPrice, BigDecimal minimumBalance, BigDecimal lowBalanceFee,
                Integer allowedOfflinePayouts, Integer offlineMultiplierDecrement, Integer withdrawalMultiplierDecrement,
                Integer playerBankAccountLimit, Boolean reimburseAccountCreation, Boolean payOnLowBalance,
                List<Integer> interestMultipliers, Set<LocalTime> interestPayoutTimes) {
        super(id);
        this.name = name;
        this.owner = owner;
        this.region = region;
        this.co_owners.addAll(coOwners);
        setInterestRate(interestRate);
        setAccountCreationPrice(accountCreationPrice);
        setMinimumBalance(minimumBalance);
        setLowBalanceFee(lowBalanceFee);
        setAllowedOfflinePayouts(allowedOfflinePayouts);
        setOfflineMultiplierDecrement(offlineMultiplierDecrement);
        setWithdrawalMultiplierDecrement(withdrawalMultiplierDecrement);
        setPlayerBankAccountLimit(playerBankAccountLimit);
        setReimburseAccountCreation(reimburseAccountCreation);
        setPayOnLowBalance(payOnLowBalance);
        setInterestMultipliers(interestMultipliers);
        setInterestPayoutTimes(interestPayoutTimes);
    }
    
    public Bank(String name, OfflinePlayer owner, BankRegion region) {
        this.name = name;
        this.owner = owner;
        this.region = region;
    }

    public Set<Account> getAccounts() {
        return new HashSet<>(accounts);
    }

    public int getNumberOfAccounts() {
        return accounts.size();
    }

    public void addAccount(Account account) {
        accounts.add(account);
        notifyObservers();
    }

    public void removeAccount(Account account) {
        accounts.remove(account);
        notifyObservers();
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
        return accounts.stream()
                .flatMap(account -> account.getTrustedPlayers().stream())
                .collect(Collectors.toSet());
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
        notifyObservers();
    }

    public Boolean reimbursesAccountCreation() {
        return (Boolean) policies.get(Policy.REIMBURSE_ACCOUNT_CREATION);
    }

    public void setReimburseAccountCreation(Boolean reimburseAccountCreation) {
        set(Policy.REIMBURSE_ACCOUNT_CREATION, reimburseAccountCreation);
    }

    public Boolean paysOnLowBalance() {
        return (Boolean) policies.get(Policy.PAY_ON_LOW_BALANCE);
    }

    public void setPayOnLowBalance(Boolean payOnLowBalance) {
        set(Policy.PAY_ON_LOW_BALANCE, payOnLowBalance);
    }

    public BigDecimal getInterestRate() {
        return (BigDecimal) policies.get(Policy.INTEREST_RATE);
    }

    public void setInterestRate(BigDecimal interestRate) {
        set(Policy.INTEREST_RATE, interestRate);
    }

    public BigDecimal getAccountCreationPrice() {
        return (BigDecimal) policies.get(Policy.ACCOUNT_CREATION_PRICE);
    }

    public void setAccountCreationPrice(BigDecimal accountCreationPrice) {
        set(Policy.ACCOUNT_CREATION_PRICE, accountCreationPrice);
    }

    public BigDecimal getMinimumBalance() {
        return (BigDecimal) policies.get(Policy.MINIMUM_BALANCE);
    }

    public void setMinimumBalance(BigDecimal minimumBalance) {
        set(Policy.MINIMUM_BALANCE, minimumBalance);
    }

    public BigDecimal getLowBalanceFee() {
        return (BigDecimal) policies.get(Policy.LOW_BALANCE_FEE);
    }

    public void setLowBalanceFee(BigDecimal lowBalanceFee) {
        set(Policy.LOW_BALANCE_FEE, lowBalanceFee);
    }

    public Integer getAllowedOfflinePayouts() {
        return (Integer) policies.get(Policy.ALLOWED_OFFLINE_PAYOUTS);
    }

    public void setAllowedOfflinePayouts(Integer allowedOfflinePayouts) {
        set(Policy.ALLOWED_OFFLINE_PAYOUTS, allowedOfflinePayouts);
    }

    public Integer getOfflineMultiplierDecrement() {
        return (Integer) policies.get(Policy.OFFLINE_MULTIPLIER_DECREMENT);
    }

    public void setOfflineMultiplierDecrement(Integer offlineMultiplierDecrement) {
        set(Policy.OFFLINE_MULTIPLIER_DECREMENT, offlineMultiplierDecrement);
    }

    public Integer getWithdrawalMultiplierDecrement() {
        return (Integer) policies.get(Policy.WITHDRAWAL_MULTIPLIER_DECREMENT);
    }

    public void setWithdrawalMultiplierDecrement(Integer withdrawalMultiplierDecrement) {
        set(Policy.WITHDRAWAL_MULTIPLIER_DECREMENT, withdrawalMultiplierDecrement);
    }

    public Integer getPlayerBankAccountLimit() {
        return (Integer) policies.get(Policy.PLAYER_BANK_ACCOUNT_LIMIT);
    }

    public void setPlayerBankAccountLimit(Integer playerBankAccountLimit) {
        set(Policy.PLAYER_BANK_ACCOUNT_LIMIT, playerBankAccountLimit);
    }

    @SuppressWarnings("unchecked")
    public List<Integer> getInterestMultipliers() {
        return (List<Integer>) policies.get(Policy.INTEREST_MULTIPLIERS);
    }

    public void setInterestMultipliers(List<Integer> interestMultipliers) {
        set(Policy.INTEREST_MULTIPLIERS, interestMultipliers);
    }

    @SuppressWarnings("unchecked")
    public Set<LocalTime> getInterestPayoutTimes() {
        return (Set<LocalTime>) policies.get(Policy.INTEREST_PAYOUT_TIMES);
    }

    public void setInterestPayoutTimes(Set<LocalTime> interestPayoutTimes) {
        set(Policy.INTEREST_PAYOUT_TIMES, interestPayoutTimes);
    }
    
    private void set(Policy policy, Object value) {
        policies.put(policy, value);
        notifyObservers();
    }

    public String getName() {
        return name.replaceAll("&[0-9A-FK-OR]", "");
    }

    public void setName(String name) {
        this.name = name;
        notifyObservers();
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
        notifyObservers();
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
        notifyObservers();
    }

    @Override
    public void untrustPlayer(OfflinePlayer player) {
        if (player == null)
            return;
        getCoOwners().remove(player);
        notifyObservers();
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
    
    private final Set<Observer> observers = new HashSet<>();
    @Override
    public Set<Observer> getObservers() {
        return observers;
    }

    @Override
    public String toString() {
        return "Bank{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", owner=" + (owner == null ? "none" : owner.getName()) +
                ", region=" + region +
                ", accounts=" + accounts +
                ", policies=" + policies +
                '}';
    }
    
}
