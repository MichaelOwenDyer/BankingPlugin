package com.monst.bankingplugin.banking.bank;

import com.monst.bankingplugin.banking.BankingEntity;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.geo.selections.Selection;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.banking.Nameable;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
				new BankConfig()
		);
	}

	/**
	 * Re-creates a bank that was stored in the {@link com.monst.bankingplugin.sql.Database}.
	 */
	public static Bank recreate(int id, String name, OfflinePlayer owner, Set<OfflinePlayer> coowners,
								Selection selection, BankConfig bankConfig) {
		return new Bank(
				id,
				name,
				owner,
				coowners,
				selection,
				bankConfig
		);
	}

	private final Set<Account> accounts;
	private final BankConfig bankConfig;
	private Selection selection;

	/**
	 * @param id the bank ID {@link BankingEntity}
	 * @param name the name of the bank {@link Nameable}
	 * @param owner the owner of the bank {@link BankingEntity}
	 * @param coowners the co-owners of the bank {@link BankingEntity}
	 * @param selection the {@link Selection} representing the bounds of the bank
	 * @param bankConfig the {@link BankConfig} of the bank
	 */
	private Bank(int id, String name, OfflinePlayer owner, Set<OfflinePlayer> coowners,
				 Selection selection, BankConfig bankConfig) {

		this.id = id;
		this.owner = owner;
		this.coowners = coowners;
		this.name = name;
		this.selection = selection;
		this.bankConfig = bankConfig;
		this.accounts = new HashSet<>();

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
		plugin.debugf("Adding account #%d to bank #%d", account.getID(), getID());
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
		plugin.debugf("Removing account #%d from bank #%d", account.getID(), getID());
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

	public boolean set(BankField field, String value, Callback<String> callback) {
		return bankConfig.set(field, value, callback.andThen(result -> {
			notifyObservers();
			getAccounts().forEach(Account::notifyObservers);
		}));
	}

	public String getFormatted(BankField field) {
		return bankConfig.getFormatted(field);
	}

	public <T> T get(BankField field) {
		return get(field, false);
	}

	public <T> T get(BankField field, boolean ignoreConfig) {
		return bankConfig.get(field, ignoreConfig);
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
		List<BigDecimal> orderedBalances = getBalancesByOwner()
				.values()
				.stream()
				.sorted(BigDecimal::compareTo)
				.collect(Collectors.toList());
		BigDecimal valueSum = BigDecimal.ZERO;
		BigDecimal weightedValueSum = BigDecimal.ZERO;
		for (int i = 0; i < orderedBalances.size(); i++) {
			valueSum = valueSum.add(orderedBalances.get(i));
			weightedValueSum = weightedValueSum.add(orderedBalances.get(i).multiply(BigDecimal.valueOf(i + 1)));
		}
		valueSum = valueSum.multiply(BigDecimal.valueOf(orderedBalances.size()));
		weightedValueSum = weightedValueSum.multiply(BigDecimal.valueOf(2));
		if (valueSum.signum() == 0)
			return 0;
		BigDecimal leftSide = weightedValueSum.divide(valueSum, 10, RoundingMode.HALF_EVEN);
		BigDecimal rightSide = BigDecimal.valueOf((orderedBalances.size() + 1) / orderedBalances.size());
		return leftSide.subtract(rightSide).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
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
			coowners.add(prevOwner);
		untrustPlayer(owner); // Remove from co-owners if new owner was a co-owner
		notifyObservers();
	}

	@Override
	public String toConsolePrintout() {

		return Stream.of(
				"\"" + ChatColor.RED + getColorizedName() + ChatColor.GRAY + "\" (#" + getID() + ")",
				"Owner: " + getOwnerDisplayName(),
				"Co-owners: " + Utils.map(getCoOwners(), OfflinePlayer::getName).toString(),
				"Interest rate: " + ChatColor.GREEN + getFormatted(BankField.INTEREST_RATE),
				"Multipliers: " + Utils.map(Utils.stackList(get(BankField.MULTIPLIERS)),
						list -> "" + list.get(0) + (list.size() > 1 ? "(x" + list.size() + ")" : "")).toString(),
				"Account creation price: " + ChatColor.GREEN + getFormatted(BankField.ACCOUNT_CREATION_PRICE),
				"Offline payouts: " + ChatColor.AQUA + getFormatted(BankField.ALLOWED_OFFLINE_PAYOUTS),
						" (" + ChatColor.AQUA + getFormatted(BankField.ALLOWED_OFFLINE_PAYOUTS_BEFORE_RESET) + ChatColor.GRAY + " before multiplier reset)",
				"Initial payout delay: " + ChatColor.AQUA + getFormatted(BankField.INITIAL_INTEREST_DELAY),
				"Minimum balance: " + ChatColor.GREEN + getFormatted(BankField.MINIMUM_BALANCE),
						" (" + ChatColor.RED + getFormatted(BankField.LOW_BALANCE_FEE) + ChatColor.GRAY + " fee)",
						" (" + ChatColor.RED + getFormatted(BankField.LOW_BALANCE_FEE) + ChatColor.GRAY + " fee)",
				"Accounts: " + ChatColor.AQUA + getAccounts().size(),
				"Total value: " + ChatColor.GREEN + Utils.format(getTotalValue()),
				"Average account value: " + ChatColor.GREEN + Utils.format(getTotalValue().divide(BigDecimal.valueOf(getAccounts().size()), BigDecimal.ROUND_HALF_EVEN)),
				"Equality score: " + getGiniCoefficient(),
				"Location: " + ChatColor.AQUA + getSelection().getCoordinates()
		).collect(Collectors.joining(", ", "" + ChatColor.GRAY, ""));
	}

	@Override
	public String toString() {
		   return "Bank ID: " + getID() + ", "
				+ "Name: " + getName() + " (Raw: " + getRawName() + "), "
				+ "Owner: " + (isPlayerBank() ? getOwner().getName() : "ADMIN") + ", "
				+ "Number of accounts: " + getAccounts().size() + ", "
				+ "Total value: " + Utils.format(getTotalValue()) + ", "
				+ "Equality score: " + getGiniCoefficient() + ", "
				+ "Selection type: " + getSelection().getType() + ", "
				+ "Location: " + getSelection().getCoordinates();
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
