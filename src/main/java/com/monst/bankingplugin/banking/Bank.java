package com.monst.bankingplugin.banking;

import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.config.values.OverriddenValue;
import com.monst.bankingplugin.exceptions.parse.ArgumentParseException;
import com.monst.bankingplugin.geo.regions.BankRegion;
import com.monst.bankingplugin.utils.QuickMath;
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
	 * Creates a new bank.
	 */
	public static Bank mint(String name, OfflinePlayer owner, BankRegion region) {
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
	 * Re-creates a bank that was stored in the database.
	 */
	public static Bank recreate(int id, String name, OfflinePlayer owner, Set<OfflinePlayer> coowners,
								BankRegion region, Boolean countInterestDelayOffline, Boolean reimburseAccountCreation, Boolean payOnLowBalance,
								Double interestRate, Double accountCreationPrice, Double minimumBalance, Double lowBalanceFee,
								Integer initialInterestDelay, Integer allowedOfflinePayouts, Integer offlineMultiplierDecrement,
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
	private final OverriddenValue<Double> interestRate;
	private final OverriddenValue<Double> accountCreationPrice;
	private final OverriddenValue<Double> minimumBalance;
	private final OverriddenValue<Double> lowBalanceFee;
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
	 *
	 */
	private Bank(int id, String name, OfflinePlayer owner, Set<OfflinePlayer> coowners, BankRegion region,
				 Boolean countInterestDelayOffline, Boolean reimburseAccountCreation, Boolean payOnLowBalance,
				 Double interestRate, Double accountCreationPrice, Double minimumBalance, Double lowBalanceFee,
				 Integer initialInterestDelay, Integer allowedOfflinePayouts, Integer offlineMultiplierDecrement,
				 Integer withdrawalMultiplierDecrement, Integer playerBankAccountLimit,
				 List<Integer> multipliers, Set<LocalTime> interestPayoutTimes) {

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

	/**
	 * Get a copy of all accounts that fulfill a certain predicate
	 * @param filter the predicate to be applied to each account at this bank
	 * @return a {@link Set} containing the filtered accounts
	 */
	public Set<Account> getAccounts(Predicate<? super Account> filter) {
		return Utils.filter(getAccounts(), filter);
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
		plugin.getAccountRepository().notifyObservers();
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
		plugin.getAccountRepository().notifyObservers();
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
		return accounts.isEmpty() ? BigDecimal.ZERO : QuickMath.divide(getTotalValue(), accounts.size());
	}

	/**
	 * @return a {@link Map<OfflinePlayer>} containing
	 * all accounts at this bank grouped by owner
	 */
	public Map<OfflinePlayer, List<Account>> getAccountsByOwner() {
		return accounts.stream().collect(Collectors.groupingBy(Account::getOwner));
	}

	/**
	 * @return a {@link Map<OfflinePlayer>} containing
	 * all account owners at this bank and their total account balances
	 */
	public Map<OfflinePlayer, BigDecimal> getBalancesByOwner() {
		return getAccountsByOwner().entrySet().stream().collect(
				Collectors.toMap(
						Map.Entry::getKey,
						entry -> entry.getValue().stream()
								.map(Account::getBalance)
								.reduce(BigDecimal.ZERO, BigDecimal::add)
				)
		);
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

	public OverriddenValue<Boolean> getCountInterestDelayOffline() {
		return countInterestDelayOffline;
	}

	public OverriddenValue<Boolean> getReimburseAccountCreation() {
		return reimburseAccountCreation;
	}

	public OverriddenValue<Boolean> getPayOnLowBalance() {
		return payOnLowBalance;
	}

	public OverriddenValue<Double> getInterestRate() {
		return interestRate;
	}

	public OverriddenValue<Double> getAccountCreationPrice() {
		return accountCreationPrice;
	}

	public OverriddenValue<Double> getMinimumBalance() {
		return minimumBalance;
	}

	public OverriddenValue<Double> getLowBalanceFee() {
		return lowBalanceFee;
	}

	public OverriddenValue<Integer> getInitialInterestDelay() {
		return initialInterestDelay;
	}

	public OverriddenValue<Integer> getAllowedOfflinePayouts() {
		return allowedOfflinePayouts;
	}

	public OverriddenValue<Integer> getOfflineMultiplierDecrement() {
		return offlineMultiplierDecrement;
	}

	public OverriddenValue<Integer> getWithdrawalMultiplierDecrement() {
		return withdrawalMultiplierDecrement;
	}

	public OverriddenValue<Integer> getPlayerBankAccountLimit() {
		return playerBankAccountLimit;
	}

	public OverriddenValue<List<Integer>> getMultipliers() {
		return multipliers;
	}

	public OverriddenValue<Set<LocalTime>> getInterestPayoutTimes() {
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
	 *
	 * @return G = ( 2 * (sum(i...n) i * n[i].getBalance()) / n * (sum(i...n) n[i].getBalance()) ) - ( n + 1 / n )
	 */
	public double getGiniCoefficient() {
		if (accounts.isEmpty())
			return 0;
		BigDecimal totalValue = getTotalValue();
		if (totalValue.signum() == 0)
			return 0;
		List<BigDecimal> orderedBalances = getBalancesByOwner()
				.values()
				.stream()
				.sorted()
				.collect(Collectors.toCollection(ArrayList::new));
		totalValue = QuickMath.multiply(totalValue, orderedBalances.size());
		BigDecimal weightedValueSum = BigDecimal.ZERO;
		for (int i = 0; i < orderedBalances.size(); i++)
			weightedValueSum = weightedValueSum.add(QuickMath.multiply(orderedBalances.get(i), i + 1));
		weightedValueSum = QuickMath.multiply(weightedValueSum, 2);
		BigDecimal leftSide = weightedValueSum.divide(totalValue, 10, RoundingMode.HALF_EVEN);
		BigDecimal rightSide = BigDecimal.valueOf((orderedBalances.size() + 1) / orderedBalances.size());
		return QuickMath.scale(leftSide.subtract(rightSide)).doubleValue();
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
		plugin.getBankRepository().notifyObservers();
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
				"Co-owners: " + Utils.map(getCoOwners(), OfflinePlayer::getName).toString(),
				"Interest rate: " + ChatColor.GREEN + getInterestRate().getFormatted(),
				"Multipliers: " + Utils.map(Utils.stackList(getMultipliers().get()),
						list -> "" + list.get(0) + (list.size() > 1 ? "(x" + list.size() + ")" : "")).toString(),
				"Account creation price: " + ChatColor.GREEN + getAccountCreationPrice().getFormatted(),
				"Offline payouts: " + ChatColor.AQUA + getAllowedOfflinePayouts().getFormatted(),
				"Initial payout delay: " + ChatColor.AQUA + getInitialInterestDelay().getFormatted(),
				"Minimum balance: " + ChatColor.GREEN + getMinimumBalance().getFormatted(),
						" (" + ChatColor.RED + getLowBalanceFee().getFormatted() + ChatColor.GRAY + " fee)",
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
