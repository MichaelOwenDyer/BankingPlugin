package com.monst.bankingplugin.banking.bank;

import com.monst.bankingplugin.banking.BankingEntity;
import com.monst.bankingplugin.banking.Nameable;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.configuration.*;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.exceptions.ArgumentParseException;
import com.monst.bankingplugin.geo.selections.Selection;
import com.monst.bankingplugin.utils.Callback;
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
	public static Bank mint(String name, OfflinePlayer owner, Selection selection) {
		return new Bank(
				-1,
				name,
				owner,
				new HashSet<>(),
				selection,
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
				null,
				null
		);
	}

	/**
	 * Re-creates a bank that was stored in the {@link com.monst.bankingplugin.sql.Database}.
	 */
	public static Bank recreate(int id, String name, OfflinePlayer owner, Set<OfflinePlayer> coowners,
								Selection selection, Boolean countInterestDelayOffline, Boolean reimburseAccountCreation, Boolean payOnLowBalance,
								Double interestRate, Double accountCreationPrice, Double minimumBalance, Double lowBalanceFee,
								Integer initialInterestDelay, Integer allowedOfflinePayouts, Integer allowedOfflinePayoutsUntilReset,
								Integer offlineMultiplierDecrement, Integer withdrawalMultiplierDecrement, Integer playerBankAccountLimit,
								List<Integer> multipliers, List<LocalTime> interestPayoutTimes) {
		return new Bank(
				id,
				name,
				owner,
				new HashSet<>(coowners),
				selection,
				countInterestDelayOffline,
				reimburseAccountCreation,
				payOnLowBalance,
				interestRate,
				accountCreationPrice,
				minimumBalance,
				lowBalanceFee,
				initialInterestDelay,
				allowedOfflinePayouts,
				allowedOfflinePayoutsUntilReset,
				offlineMultiplierDecrement,
				withdrawalMultiplierDecrement,
				playerBankAccountLimit,
				multipliers,
				interestPayoutTimes
		);
	}

	private final Set<Account> accounts;
	private Selection selection;

	private final CountInterestDelayOffline countInterestDelayOffline;
	private final ReimburseAccountCreation reimburseAccountCreation;
	private final PayOnLowBalance payOnLowBalance;
	private final InterestRate interestRate;
	private final AccountCreationPrice accountCreationPrice;
	private final MinimumBalance minimumBalance;
	private final LowBalanceFee lowBalanceFee;
	private final InitialInterestDelay initialInterestDelay;
	private final AllowedOfflinePayouts allowedOfflinePayouts;
	private final AllowedOfflinePayoutsBeforeReset allowedOfflinePayoutsBeforeReset;
	private final OfflineMultiplierDecrement offlineMultiplierDecrement;
	private final WithdrawalMultiplierDecrement withdrawalMultiplierDecrement;
	private final PlayerBankAccountLimit playerBankAccountLimit;
	private final Multipliers multipliers;
	private final InterestPayoutTimes interestPayoutTimes;

	/**
	 * @param id the bank ID {@link BankingEntity}
	 * @param name the name of the bank {@link Nameable}
	 * @param owner the owner of the bank {@link BankingEntity}
	 * @param coowners the co-owners of the bank {@link BankingEntity}
	 * @param selection the {@link Selection} representing the bounds of the bank
	 *
	 */
	private Bank(int id, String name, OfflinePlayer owner, Set<OfflinePlayer> coowners,
				 Selection selection, Boolean countInterestDelayOffline, Boolean reimburseAccountCreation, Boolean payOnLowBalance,
				 Double interestRate, Double accountCreationPrice, Double minimumBalance, Double lowBalanceFee,
				 Integer initialInterestDelay, Integer allowedOfflinePayouts, Integer allowedOfflinePayoutsUntilReset,
				 Integer offlineMultiplierDecrement, Integer withdrawalMultiplierDecrement, Integer playerBankAccountLimit,
				 List<Integer> multipliers, List<LocalTime> interestPayoutTimes) {

		super(id, name, owner, coowners);
		this.selection = selection;
		this.accounts = new HashSet<>();
		this.countInterestDelayOffline = new CountInterestDelayOffline(countInterestDelayOffline);
		this.reimburseAccountCreation = new ReimburseAccountCreation(reimburseAccountCreation);
		this.payOnLowBalance = new PayOnLowBalance(payOnLowBalance);
		this.interestRate = new InterestRate(interestRate);
		this.accountCreationPrice = new AccountCreationPrice(accountCreationPrice);
		this.minimumBalance = new MinimumBalance(minimumBalance);
		this.lowBalanceFee = new LowBalanceFee(lowBalanceFee);
		this.initialInterestDelay = new InitialInterestDelay(initialInterestDelay);
		this.allowedOfflinePayouts = new AllowedOfflinePayouts(allowedOfflinePayouts);
		this.allowedOfflinePayoutsBeforeReset = new AllowedOfflinePayoutsBeforeReset(allowedOfflinePayoutsUntilReset);
		this.offlineMultiplierDecrement = new OfflineMultiplierDecrement(offlineMultiplierDecrement);
		this.withdrawalMultiplierDecrement = new WithdrawalMultiplierDecrement(withdrawalMultiplierDecrement);
		this.playerBankAccountLimit = new PlayerBankAccountLimit(playerBankAccountLimit);
		this.multipliers = new Multipliers(multipliers);
		this.interestPayoutTimes = new InterestPayoutTimes(interestPayoutTimes);

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
		return getAccounts().stream().map(Account::getBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	/**
	 * @return a {@link Map<OfflinePlayer>} containing
	 * all accounts at this bank grouped by owner
	 */
	public Map<OfflinePlayer, List<Account>> getAccountsByOwner() {
		return getAccounts().stream().collect(Collectors.groupingBy(Account::getOwner));
	}

	/**
	 * @return a {@link Map<OfflinePlayer>} containing
	 * all account owners at this bank and their total account balances
	 * @see #getAccountsByOwner()
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
	 * @return a {@link Set<OfflinePlayer>} containing all account owners
	 * and account co-owners at this bank.
	 */
	public Set<OfflinePlayer> getCustomers() {
		return getAccounts().stream()
				.map(Account::getTrustedPlayers)
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
	}

	/**
	 * @return the {@link Selection} representing the bounds of this bank
	 */
	public Selection getSelection() {
		return selection;
	}

	/**
	 * @param sel the new {@link Selection} to represent the bounds of this bank
	 */
	public void setSelection(Selection sel) {
		this.selection = sel;
		notifyObservers();
	}

	public CountInterestDelayOffline getCountInterestDelayOffline() {
		return countInterestDelayOffline;
	}

	public ReimburseAccountCreation getReimburseAccountCreation() {
		return reimburseAccountCreation;
	}

	public PayOnLowBalance getPayOnLowBalance() {
		return payOnLowBalance;
	}

	public InterestRate getInterestRate() {
		return interestRate;
	}

	public AccountCreationPrice getAccountCreationPrice() {
		return accountCreationPrice;
	}

	public MinimumBalance getMinimumBalance() {
		return minimumBalance;
	}

	public LowBalanceFee getLowBalanceFee() {
		return lowBalanceFee;
	}

	public InitialInterestDelay getInitialInterestDelay() {
		return initialInterestDelay;
	}

	public AllowedOfflinePayouts getAllowedOfflinePayouts() {
		return allowedOfflinePayouts;
	}

	public AllowedOfflinePayoutsBeforeReset getAllowedOfflinePayoutsBeforeReset() {
		return allowedOfflinePayoutsBeforeReset;
	}

	public OfflineMultiplierDecrement getOfflineMultiplierDecrement() {
		return offlineMultiplierDecrement;
	}

	public WithdrawalMultiplierDecrement getWithdrawalMultiplierDecrement() {
		return withdrawalMultiplierDecrement;
	}

	public PlayerBankAccountLimit getPlayerBankAccountLimit() {
		return playerBankAccountLimit;
	}

	public Multipliers getMultipliers() {
		return multipliers;
	}

	public InterestPayoutTimes getInterestPayoutTimes() {
		return interestPayoutTimes;
	}

	public ConfigurationOption<?> get(BankField field) {
		switch (field) {
			case COUNT_INTEREST_DELAY_OFFLINE:
				return getCountInterestDelayOffline();
			case REIMBURSE_ACCOUNT_CREATION:
				return getReimburseAccountCreation();
			case PAY_ON_LOW_BALANCE:
				return getPayOnLowBalance();
			case INTEREST_RATE:
				return getInterestRate();
			case ACCOUNT_CREATION_PRICE:
				return getAccountCreationPrice();
			case MINIMUM_BALANCE:
				return getMinimumBalance();
			case LOW_BALANCE_FEE:
				return getLowBalanceFee();
			case INITIAL_INTEREST_DELAY:
				return getInitialInterestDelay();
			case ALLOWED_OFFLINE_PAYOUTS:
				return getAllowedOfflinePayouts();
			case ALLOWED_OFFLINE_PAYOUTS_BEFORE_MULTIPLIER_RESET:
				return getAllowedOfflinePayoutsBeforeReset();
			case OFFLINE_MULTIPLIER_DECREMENT:
				return getOfflineMultiplierDecrement();
			case WITHDRAWAL_MULTIPLIER_DECREMENT:
				return getWithdrawalMultiplierDecrement();
			case PLAYER_BANK_ACCOUNT_LIMIT:
				return getPlayerBankAccountLimit();
			case MULTIPLIERS:
				return getMultipliers();
			case INTEREST_PAYOUT_TIMES:
				return getInterestPayoutTimes();
			default:
				return null;
		}
	}

	public boolean set(BankField field, String input) throws ArgumentParseException {
		boolean overrideCompliant = get(field).set(input);
		notifyObservers();
		getAccounts().forEach(Account::notifyObservers);
		return overrideCompliant;
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
	 * inequality among all n accounts at the bank.
	 *
	 * @return G = ( 2 * (sum(i...n) i * n[i].getBalance()) / n * (sum(i...n) n[i].getBalance()) ) - ( n + 1 / n )
	 */
	public double getGiniCoefficient() {
		if (getAccounts().isEmpty())
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
		plugin.getBankRepository().add(this, true); // Update bank in database
		notifyObservers();
		getAccounts().forEach(Account::notifyObservers);
		plugin.getBankRepository().notifyObservers();
	}

	@Override
	public void setOwner(OfflinePlayer newOwner) {
		OfflinePlayer prevOwner = getOwner();
		owner = newOwner;
		if (Config.trustOnTransfer)
			trustPlayer(prevOwner);
		untrustPlayer(owner); // Remove from co-owners if new owner was a co-owner
		notifyObservers();
	}

	@Override
	public void trustPlayer(OfflinePlayer p) {
		if (p == null)
			return;
		super.trustPlayer(p);
		plugin.getDatabase().addCoOwner(this, p, Callback.blank());
	}

	@Override
	public void untrustPlayer(OfflinePlayer p) {
		if (p == null)
			return;
		super.untrustPlayer(p);
		plugin.getDatabase().removeCoOwner(this, p, Callback.blank());
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
						" (" + ChatColor.AQUA + getAllowedOfflinePayoutsBeforeReset().getFormatted() + ChatColor.GRAY + " before multiplier reset)",
				"Initial payout delay: " + ChatColor.AQUA + getInitialInterestDelay().getFormatted(),
				"Minimum balance: " + ChatColor.GREEN + getMinimumBalance().getFormatted(),
						" (" + ChatColor.RED + getLowBalanceFee().getFormatted() + ChatColor.GRAY + " fee)",
				"Accounts: " + ChatColor.AQUA + getAccounts().size(),
				"Total value: " + Utils.formatAndColorize(getTotalValue()),
				"Average account value: " + Utils.formatAndColorize(QuickMath.divide(getTotalValue(), getAccounts().size())),
				"Equality score: " + getGiniCoefficient(),
				"Location: " + ChatColor.AQUA + getSelection().getCoordinates()
		).map(s -> ChatColor.GRAY + s).collect(Collectors.joining(", ", "", ""));
	}

	@Override
	public String toString() {
		return String.join(", ",
				"Bank ID: " + getID(),
				"Name: " + getRawName(),
				"Owner: " + getOwnerName(),
				"Number of accounts: " + getAccounts().size(),
				"Total value: " + Utils.format(getTotalValue()),
				"Location: " + getSelection().getCoordinates()
		);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Bank otherBank = (Bank) o;
		return getID() != -1 && getID().equals(otherBank.getID());
	}

	@Override
	public int hashCode() {
		return getID() != -1 ? getID() : Objects.hash(owner, coowners, selection, name);
	}
}
