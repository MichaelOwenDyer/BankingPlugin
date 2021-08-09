package com.monst.bankingplugin.banking;

import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.config.values.OverriddenValue;
import com.monst.bankingplugin.exceptions.parse.ArgumentParseException;
import com.monst.bankingplugin.geo.locations.AccountLocation;
import com.monst.bankingplugin.geo.regions.BankRegion;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Bank extends BankingEntity {

	/**
	 * Opens a new bank.
	 */
	public static Bank open(String name, OfflinePlayer owner, BankRegion region) {
		return new Bank(
				-1,
				name,
				owner,
				new HashSet<>(),
				region,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null
		);
	}

	/**
	 * Reopens a bank that was stored in the database.
	 */
	public static Bank reopen(int id, String name, OfflinePlayer owner, Set<OfflinePlayer> coowners,
							  BankRegion region, Boolean countInterestDelayOffline, Boolean reimburseAccountCreation,
							  Boolean payOnLowBalance, BigDecimal interestRate, BigDecimal accountCreationPrice,
							  BigDecimal minimumBalance, BigDecimal lowBalanceFee, Integer initialInterestDelay,
							  Integer allowedOfflinePayouts, Integer offlineMultiplierDecrement,
							  Integer withdrawalMultiplierDecrement, Integer playerBankAccountLimit,
							  List<Integer> multipliers, Set<LocalTime> interestPayoutTimes) {
		return new Bank(
				id,
				name,
				owner,
				new HashSet<>(coowners),
				region,
				countInterestDelayOffline,
				reimburseAccountCreation,
				payOnLowBalance,
				interestRate,
				accountCreationPrice,
				minimumBalance,
				lowBalanceFee,
				initialInterestDelay,
				allowedOfflinePayouts,
				offlineMultiplierDecrement,
				withdrawalMultiplierDecrement,
				playerBankAccountLimit,
				multipliers,
				interestPayoutTimes
		);
	}

	private final Set<Account> accounts;
	private BankRegion region;

	private final OverriddenValue<Boolean> countInterestDelayOffline;
	private final OverriddenValue<Boolean> reimburseAccountCreation;
	private final OverriddenValue<Boolean> payOnLowBalance;
	private final OverriddenValue<BigDecimal> interestRate;
	private final OverriddenValue<BigDecimal> accountCreationPrice;
	private final OverriddenValue<BigDecimal> minimumBalance;
	private final OverriddenValue<BigDecimal> lowBalanceFee;
	private final OverriddenValue<Integer> initialInterestDelay;
	private final OverriddenValue<Integer> allowedOfflinePayouts;
	private final OverriddenValue<Integer> offlineMultiplierDecrement;
	private final OverriddenValue<Integer> withdrawalMultiplierDecrement;
	private final OverriddenValue<Integer> playerBankAccountLimit;
	private final OverriddenValue<List<Integer>> multipliers;
	private final OverriddenValue<Set<LocalTime>> interestPayoutTimes;

	/**
	 * @param id the bank ID {@link BankingEntity}
	 * @param name the name of the bank {@link Nameable}
	 * @param owner the owner of the bank {@link BankingEntity}
	 * @param coowners the co-owners of the bank {@link BankingEntity}
	 * @param region the {@link BankRegion} representing the bounds of the bank
	 */
	private Bank(int id, String name, OfflinePlayer owner, Set<OfflinePlayer> coowners, BankRegion region,
				 Boolean countInterestDelayOffline, Boolean reimburseAccountCreation, Boolean payOnLowBalance,
				 BigDecimal interestRate, BigDecimal accountCreationPrice, BigDecimal minimumBalance,
				 BigDecimal lowBalanceFee, Integer initialInterestDelay, Integer allowedOfflinePayouts,
				 Integer offlineMultiplierDecrement, Integer withdrawalMultiplierDecrement,
				 Integer playerBankAccountLimit, List<Integer> multipliers, Set<LocalTime> interestPayoutTimes) {

		super(id, name, owner, coowners);
		this.region = region;
		this.accounts = new HashSet<>();
		this.countInterestDelayOffline = Config.countInterestDelayOffline.override(countInterestDelayOffline);
		this.reimburseAccountCreation = Config.reimburseAccountCreation.override(reimburseAccountCreation);
		this.payOnLowBalance = Config.payOnLowBalance.override(payOnLowBalance);
		this.interestRate = Config.interestRate.override(interestRate);
		this.accountCreationPrice = Config.accountCreationPrice.override(accountCreationPrice);
		this.minimumBalance = Config.minimumBalance.override(minimumBalance);
		this.lowBalanceFee = Config.lowBalanceFee.override(lowBalanceFee);
		this.initialInterestDelay = Config.initialInterestDelay.override(initialInterestDelay);
		this.allowedOfflinePayouts = Config.allowedOfflinePayouts.override(allowedOfflinePayouts);
		this.offlineMultiplierDecrement = Config.offlineMultiplierDecrement.override(offlineMultiplierDecrement);
		this.withdrawalMultiplierDecrement = Config.withdrawalMultiplierDecrement.override(withdrawalMultiplierDecrement);
		this.playerBankAccountLimit = Config.playerBankAccountLimit.override(playerBankAccountLimit);
		this.multipliers = Config.multipliers.override(multipliers);
		this.interestPayoutTimes = Config.interestPayoutTimes.override(this, interestPayoutTimes);

	}

	/**
	 * @return a {@link Collection<Account>} containing all accounts at this bank
	 */
	public Set<Account> getAccounts() {
		return new HashSet<>(accounts);
	}

	public boolean hasAccounts() {
		return !accounts.isEmpty();
	}

	/**
	 * Get a copy of all accounts that fulfill a certain predicate
	 * @param filter the predicate to be applied to each account at this bank
	 * @return a {@link Set} containing the filtered accounts
	 */
	public Set<Account> getAccounts(Predicate<? super Account> filter) {
		return Utils.filter(getAccounts(), filter);
	}

	public Account openAccount(OfflinePlayer holder, AccountLocation loc) {
		return Account.open(this, holder, loc);
	}

	/**
	 * Adds an account to this bank.
	 * @param account the account to be added
	 */
	public void addAccount(Account account) {
		if (account == null)
			return;
		accounts.add(account);
		notifyObservers();
	}

	/**
	 * Removes an account from this bank.
	 * @param account the account to be removed
	 */
	public void removeAccount(Account account) {
		if (account == null)
			return;
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
				.map(Account::getTrustedPlayers)
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
	}

	/**
	 * @return the {@link BankRegion} representing the bounds of this bank
	 */
	public BankRegion getRegion() {
		return region;
	}

	/**
	 * @param region the new {@link BankRegion} to represent the bounds of this bank
	 */
	public void setRegion(BankRegion region) {
		this.region = region;
		notifyObservers();
	}

	public OverriddenValue<Boolean> countInterestDelayOffline() {
		return countInterestDelayOffline;
	}

	public OverriddenValue<Boolean> reimburseAccountCreation() {
		return reimburseAccountCreation;
	}

	public OverriddenValue<Boolean> payOnLowBalance() {
		return payOnLowBalance;
	}

	public OverriddenValue<BigDecimal> interestRate() {
		return interestRate;
	}

	public OverriddenValue<BigDecimal> accountCreationPrice() {
		return accountCreationPrice;
	}

	public OverriddenValue<BigDecimal> minimumBalance() {
		return minimumBalance;
	}

	public OverriddenValue<BigDecimal> lowBalanceFee() {
		return lowBalanceFee;
	}

	public OverriddenValue<Integer> initialInterestDelay() {
		return initialInterestDelay;
	}

	public OverriddenValue<Integer> allowedOfflinePayouts() {
		return allowedOfflinePayouts;
	}

	public OverriddenValue<Integer> offlineMultiplierDecrement() {
		return offlineMultiplierDecrement;
	}

	public OverriddenValue<Integer> withdrawalMultiplierDecrement() {
		return withdrawalMultiplierDecrement;
	}

	public OverriddenValue<Integer> playerBankAccountLimit() {
		return playerBankAccountLimit;
	}

	public OverriddenValue<List<Integer>> multipliers() {
		return multipliers;
	}

	public OverriddenValue<Set<LocalTime>> interestPayoutTimes() {
		return interestPayoutTimes;
	}

	public OverriddenValue<?> get(BankField field) {
		switch (field) {
			case COUNT_INTEREST_DELAY_OFFLINE:
				return countInterestDelayOffline;
			case REIMBURSE_ACCOUNT_CREATION:
				return reimburseAccountCreation;
			case PAY_ON_LOW_BALANCE:
				return payOnLowBalance;
			case INTEREST_RATE:
				return interestRate;
			case ACCOUNT_CREATION_PRICE:
				return accountCreationPrice;
			case MINIMUM_BALANCE:
				return minimumBalance;
			case LOW_BALANCE_FEE:
				return lowBalanceFee;
			case INITIAL_INTEREST_DELAY:
				return initialInterestDelay;
			case ALLOWED_OFFLINE_PAYOUTS:
				return allowedOfflinePayouts;
			case OFFLINE_MULTIPLIER_DECREMENT:
				return offlineMultiplierDecrement;
			case WITHDRAWAL_MULTIPLIER_DECREMENT:
				return withdrawalMultiplierDecrement;
			case PLAYER_BANK_ACCOUNT_LIMIT:
				return playerBankAccountLimit;
			case INTEREST_MULTIPLIERS:
				return multipliers;
			case INTEREST_PAYOUT_TIMES:
				return interestPayoutTimes;
			default:
				throw new IllegalStateException("Unknown bank field!");
		}
	}

	public boolean set(BankField field, String input) throws ArgumentParseException {
		boolean isOverrideCompliant = get(field).set(input);
		notifyObservers();
		notifyAccountObservers();
		return isOverrideCompliant;
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
	 * (in)equality among all n accounts at the bank.
	 * @return the Gini coefficient
	 */
	public double getGiniCoefficient() {
		if (getAccountHolders().size() <= 1)
			return 0.0;
		BigDecimal totalValue = getTotalValue();
		if (totalValue.signum() == 0)
			return 0.0;
		BigDecimal[] balances = accounts.stream()
				.collect(Collectors.toMap(Account::getOwner, Account::getBalance, BigDecimal::add))
				.values().stream().sorted().toArray(BigDecimal[]::new);
		BigDecimal topSum = BigDecimal.ZERO;
		for (int i = 1; i <= balances.length; i++) {
			BigDecimal weight = BigDecimal.valueOf((i * 2L) - balances.length - 1);
			topSum = topSum.add(balances[i - 1].multiply(weight));
		}
		BigDecimal bottomSum = totalValue.multiply(BigDecimal.valueOf(balances.length));
		return topSum.divide(bottomSum, RoundingMode.HALF_EVEN).doubleValue();
	}

	/**
	 * Sets the name of this bank and updates the value in the database.
	 * @param name the new name of this bank
	 */
	@Override
	public void setName(String name) {
		this.name = name;
		notifyObservers();
		notifyAccountObservers();
	}

	@Override
	public void setOwner(OfflinePlayer newOwner) {
		OfflinePlayer prevOwner = getOwner();
		owner = newOwner;
		if (Config.trustOnTransfer.get())
			trustPlayer(prevOwner);
		untrustPlayer(owner); // Remove from co-owners if new owner was a co-owner
		notifyObservers();
	}

	public void notifyAccountObservers() {
		accounts.forEach(Account::notifyObservers);
	}

	@Override
	public String toConsolePrintout() {
		return Stream.of(
				"\"" + ChatColor.RED + getColorizedName() + ChatColor.GRAY + "\" (#" + getID() + ")",
				"Owner: " + getOwnerDisplayName(),
				"Co-owners: " + Utils.map(getCoOwners(), OfflinePlayer::getName),
				"Interest rate: " + ChatColor.GREEN + interestRate().getFormatted(),
				"Multipliers: " + Utils.map(Utils.collapseList(multipliers().get()),
						list -> "" + list.get(0) + (list.size() > 1 ? "(x" + list.size() + ")" : "")).toString(),
				"Account creation price: " + ChatColor.GREEN + accountCreationPrice().getFormatted(),
				"Offline payouts: " + ChatColor.AQUA + allowedOfflinePayouts().getFormatted(),
				"Initial payout delay: " + ChatColor.AQUA + initialInterestDelay().getFormatted(),
				"Minimum balance: " + ChatColor.GREEN + minimumBalance().getFormatted(),
						" (" + ChatColor.RED + lowBalanceFee().getFormatted() + ChatColor.GRAY + " fee)",
				"Accounts: " + ChatColor.AQUA + accounts.size(),
				"Total account value: " + Utils.formatAndColorize(getTotalValue()),
				"Average account value: " + Utils.formatAndColorize(getAverageValue()),
				"Equality score: " + getGiniCoefficient(),
				"Location: " + ChatColor.AQUA + getRegion().getCoordinates()
		).map(s -> ChatColor.GRAY + s).collect(Collectors.joining(", "));
	}

	@Override
	public String toString() {
		return String.join(", ",
				"Bank ID: " + getID(),
				"Name: " + getRawName(),
				"Owner: " + getOwnerName(),
				"Number of accounts: " + getAccounts().size(),
				"Total value: " + Utils.format(getTotalValue()),
				"Location: " + getRegion().getCoordinates()
		);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Bank otherBank = (Bank) o;
		return getID() != -1 && Objects.equals(getID(), otherBank.getID());
	}

	@Override
	public int hashCode() {
		return getID() != -1 ? getID() : Objects.hash(owner, coowners, region, name);
	}

}
