package com.monst.bankingplugin.banking.bank;

import com.monst.bankingplugin.banking.Ownable;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.selections.Selection;
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

public class Bank extends Ownable {

	/**
	 * Creates a new admin bank.
	 */
	public static Bank mint(String name, Selection selection) {
		return new Bank(
				-1,
				name,
				null,
				new HashSet<>(),
				selection,
				BankConfig.mint(),
				BankType.ADMIN
		);
	}

	/**
	 * Creates a new player bank.
	 */
	public static Bank mint(String name, OfflinePlayer owner, Selection selection) {
		return new Bank(
				-1,
				name,
				owner,
				new HashSet<>(),
				selection,
				BankConfig.mint(),
				BankType.PLAYER
		);
	}

	/**
	 * Re-creates an admin bank that was stored in the {@link com.monst.bankingplugin.sql.Database}.
	 */
	public static Bank recreate(int id, String name, Set<OfflinePlayer> coowners,
								Selection selection, BankConfig bankConfig) {
		return new Bank(
				id,
				name,
				null,
				coowners,
				selection,
				bankConfig,
				BankType.ADMIN
		);
	}

	/**
	 * Re-creates a player bank that was stored in the {@link com.monst.bankingplugin.sql.Database}.
	 */
	public static Bank recreate(int id, String name, OfflinePlayer owner, Set<OfflinePlayer> coowners,
								Selection selection, BankConfig bankConfig) {
		return new Bank(
				id,
				name,
				owner,
				coowners,
				selection,
				bankConfig,
				BankType.PLAYER
		);
	}

	/**
	 * Banks are either owned and operated by players or by the admins.
	 * Admin banks cannot run out of money, whereas a player bank relies on its owner to pay interest to the customers.
	 */
	public enum BankType {
		PLAYER, ADMIN
	}

	private Selection selection;
	private final BankConfig bankConfig;
	private final Set<Account> accounts;
	private BankType type;

	/**
	 * @param id the bank ID {@link Ownable}
	 * @param name the name of the bank {@link Nameable}
	 * @param owner the owner of the bank {@link Ownable}
	 * @param coowners the co-owners of the bank {@link Ownable}
	 * @param selection the {@link Selection} representing the bounds of the bank
	 * @param bankConfig the {@link BankConfig} of the bank
	 * @param type the {@link BankType} of the bank
	 */
	private Bank(int id, String name, OfflinePlayer owner, Set<OfflinePlayer> coowners,
				 Selection selection, BankConfig bankConfig, BankType type) {

		this.id = id;
		this.owner = owner;
		this.coowners = coowners;
		this.name = name;
		this.selection = selection;
		this.accounts = new HashSet<>();
		this.bankConfig = bankConfig;
		this.type = type;

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

	public void updateType() {
		this.type = getOwner() == null ? BankType.ADMIN : BankType.PLAYER;
	}

	public BankType getType() {
		return type;
	}

	/**
	 * @return whether the bank is an admin bank
	 * @see BankType
	 */
	public boolean isAdminBank() {
		return type == BankType.ADMIN;
	}

	/**
	 * @return whether the bank is a player bank
	 * @see BankType
	 */
	public boolean isPlayerBank() {
		return type == BankType.PLAYER;
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
	public void transferOwnership(OfflinePlayer newOwner) {
		OfflinePlayer prevOwner = getOwner();
		owner = newOwner;
		updateType();
		if (Config.trustOnTransfer)
			coowners.add(prevOwner);
		untrustPlayer(owner); // Remove from co-owners if new owner was a co-owner
		notifyObservers();
	}

	@Override
	public String getInformation() {

		StringBuilder info = new StringBuilder(196);

		info.append("\"" + ChatColor.RED + getColorizedName() + ChatColor.GRAY + "\" (#" + getID() + ")");
		info.append(ChatColor.GRAY + "Owner: " + getOwnerDisplayName());
		info.append(ChatColor.GRAY + "Co-owners: " + (!getCoowners().isEmpty() ?
				Utils.map(getCoowners(), OfflinePlayer::getName).toString() : "[none]"));
		info.append(ChatColor.GRAY + "Interest rate: " + ChatColor.GREEN + getFormatted(BankField.INTEREST_RATE));
		info.append(ChatColor.GRAY + "Multipliers: ");
		info.append(Utils.map(Utils.stackList(get(BankField.MULTIPLIERS)),
				list -> "" + list.get(0) + (list.size() > 1 ? "(x" + list.size() + ")" : "")).toString());
		info.append(ChatColor.GRAY + "Account creation price: " + ChatColor.GREEN + getFormatted(BankField.ACCOUNT_CREATION_PRICE));
		info.append(ChatColor.GRAY + "Offline payouts: " + ChatColor.AQUA + getFormatted(BankField.ALLOWED_OFFLINE_PAYOUTS));
		info.append(ChatColor.GRAY + " (" + ChatColor.AQUA + getFormatted(BankField.ALLOWED_OFFLINE_PAYOUTS_BEFORE_RESET) + ChatColor.GRAY + " before multiplier reset)");
		info.append(ChatColor.GRAY + "Initial payout delay: " + ChatColor.AQUA + getFormatted(BankField.INITIAL_INTEREST_DELAY));
		info.append(ChatColor.GRAY + "Minimum balance: " + ChatColor.GREEN + getFormatted(BankField.MINIMUM_BALANCE));
		info.append(ChatColor.GRAY + " (" + ChatColor.RED + getFormatted(BankField.LOW_BALANCE_FEE) + ChatColor.GRAY + " fee)");
		info.append(ChatColor.GRAY + "Accounts: " + ChatColor.AQUA + getAccounts().size());
		info.append(ChatColor.GRAY + "Total value: " + ChatColor.GREEN + Utils.format(getTotalValue()));
		info.append(ChatColor.GRAY + "Average account value: " + ChatColor.GREEN + Utils.format(getTotalValue().divide(BigDecimal.valueOf(getAccounts().size()), BigDecimal.ROUND_HALF_EVEN)));
		info.append(ChatColor.GRAY + "Equality score: " + getGiniCoefficient());
		info.append(ChatColor.GRAY + "Location: " + ChatColor.AQUA + getSelection().getCoordinates());

		return info.toString();
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
		return getID() != -1 && getID() == otherBank.getID();
	}

	@Override
	public int hashCode() {
		return getID() != -1 ? getID() : Objects.hash(owner, coowners, selection, name);
	}
}
