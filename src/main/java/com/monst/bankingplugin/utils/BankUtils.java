package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.selections.Selection;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BankUtils extends Observable {

	private final BankingPlugin plugin;
	private final Map<Selection, Bank> bankSelectionMap = new HashMap<>();

    public BankUtils(BankingPlugin plugin) {
        this.plugin = plugin;
    }

    /**
	 * Gets the {@link Bank} at a given location
	 *
	 * @param location {@link Location} of the bank
	 * @return Bank at the given location or <b>null</b> if no bank is found there
	 */
	public Bank getBank(Location location) {
		for (Selection sel : bankSelectionMap.keySet())
			if (sel.contains(location))
				return bankSelectionMap.get(sel);
		return null;
    }

	/**
	 * Gets the {@link Bank} with a given selection
	 *
	 * @param selection {@link Selection} of the bank
	 * @return Bank in the given region or <b>null</b> if no bank is found there
	 */
	public Bank getBank(Selection selection) {
		return bankSelectionMap.get(selection);
	}

	public Bank lookupBank(String identifier) {
		try {
			int id = Integer.parseInt(identifier);
			return getBanks().stream().filter(b -> b.getID() == id).findFirst().orElse(null);
		} catch (NumberFormatException ignored) {}
		return getBanks().stream().filter(b -> b.getName().equalsIgnoreCase(identifier)).findFirst().orElse(null);
	}

    /**
	 * Gets all banks on the server
	 *
	 * @return A new {@link HashSet} containing all banks
	 */
	public Set<Bank> getBanks() {
		return new HashSet<>(bankSelectionMap.values());
    }

	/**
	 * Gets all banks on the server that fulfill a certain {@link Predicate}
	 *
	 * @return A new {@link HashSet} containing all banks
	 */
    public Set<Bank> getBanks(Predicate<? super Bank> filter) {
		return Utils.filter(getBanks(), filter);
	}

	/**
	 * Get the number of accounts owned by a certain player
	 *
	 * @param player Player whose accounts should be counted
	 * @return The number of accounts owned by the player
	 */
	public int getNumberOfBanks(OfflinePlayer player) {
		return getBanks(b -> b.isOwner(player)).size();
	}

	/**
	 * Add a bank
	 *
	 * @param bank          Bank to add
	 * @param addToDatabase Whether the bank should also be added to the database
	 */
	public void addBank(Bank bank, boolean addToDatabase) {
		addBank(bank, addToDatabase, null);
	}

    /**
	 * Adds a bank
	 * 
	 * @param bank          Bank to add
	 * @param addToDatabase Whether the bank should also be added to the database
	 * @param callback      Callback that - if succeeded - returns the ID the bank
	 *                      had or was given (as {@code int})
	 */
	public void addBank(Bank bank, boolean addToDatabase, Callback<Integer> callback) {
		plugin.debug("Adding/updating bank... (#" + bank.getID() + ")");

		bankSelectionMap.put(bank.getSelection(), bank);
		plugin.getScheduler().schedulePayouts(bank);

        if (addToDatabase)
			plugin.getDatabase().addBank(bank, callback);
        else if (callback != null)
				callback.callSyncResult(bank.getID());

    }

	/**
	 * Remove an bank. May not work properly if double chest doesn't exist!
	 *
	 * @param bank               Bank to remove
	 * @param removeFromDatabase Whether the bank should also be removed from the
	 *                           database
	 */
	public void removeBank(Bank bank, boolean removeFromDatabase) {
		removeBank(bank, removeFromDatabase, null);
	}

	/**
	 * Remove a bank. May not work properly if double chest doesn't exist!
	 * 
	 * @param bank               Bank to remove
	 * @param removeFromDatabase Whether the bank should also be removed from the
	 *                           database
	 * @param callback           Callback that - if succeeded - returns null
	 */
	public void removeBank(Bank bank, boolean removeFromDatabase, Callback<Void> callback) {
		plugin.debug("Removing bank (#" + bank.getID() + ")");

		for (Account account : bank.getAccounts())
			plugin.getAccountUtils().removeAccount(account, removeFromDatabase);

		bankSelectionMap.remove(bank.getSelection());
		plugin.getBankUtils().notifyObservers();

		plugin.getScheduler().unschedulePayouts(bank);

        if (removeFromDatabase)
			plugin.getDatabase().removeBank(bank, callback);
        else if (callback != null)
			callback.callSyncResult(null);
    }

	public boolean isUniqueName(String name) {
		return isUniqueNameIgnoring(name, null);
	}

	public boolean isUniqueNameIgnoring(String name, String without) {
		if (without != null)
			return getBanks(b -> !b.getName().contentEquals(without) && b.getName().contentEquals(name)).isEmpty();
		return getBanks(b -> b.getName().contentEquals(name)).isEmpty();
	}

	public Set<Selection> getOverlappingSelections(Selection sel) {
		return getOverlappingSelectionsIgnoring(sel, null);
	}

	public Set<Selection> getOverlappingSelectionsIgnoring(Selection sel, Selection ignore) {
		Set<Selection> overlappingSelections = Utils.filter(bankSelectionMap.keySet(), s -> s.overlaps(sel));
		if (ignore != null)
			overlappingSelections.remove(ignore);
		return overlappingSelections;
	}

	/**
	 * Gets the bank limits of a certain player, to see if the player is allowed to create another bank.
	 */
	public static int getBankLimit(Player player) {
		return (int) Utils.getLimit(player, Permissions.BANK_NO_LIMIT,
                Config.defaultBankLimit);
	}

	/**
	 * Gets the bank volume limit of a certain player, to see if the player is allowed to create a bank of a certain size.
	 */
	public static long getVolumeLimit(Player player) {
		return Utils.getLimit(player, Permissions.BANK_NO_SIZE_LIMIT,
                Config.maximumBankVolume);
	}

    /**
	 * Reload the plugin
	 * 
	 * @param reloadConfig        Whether the configuration should also be reloaded
	 * @param showConsoleMessages Whether messages about the language file should be
	 *                            shown in the console
	 * @param callback            Callback that - if succeeded - returns the amount
	 *                            of accounts that were reloaded (as {@code int})
	 */
	public void reload(boolean reloadConfig, boolean showConsoleMessages, Callback<ReloadResult> callback) {
		plugin.debug("Loading banks and accounts from database...");

		AccountUtils accountUtils = plugin.getAccountUtils();

		if (reloadConfig) {
			plugin.getPluginConfig().reload();
		}

		plugin.getDatabase().connect(Callback.of(plugin, result -> {
					Collection<Bank> banks = getBanks();
					Collection<Account> accounts = accountUtils.getAccounts();

					Set<Bank> reloadedBanks = new HashSet<>();
					Set<Account> reloadedAccounts = new HashSet<>();

					for (Bank bank : banks) {
						for (Account account : bank.getAccounts()) {
							accountUtils.removeAccount(account, false);
							plugin.debugf("Removed account (#%d)", account.getID());
						}
						removeBank(bank, false);
						plugin.debugf("Removed bank (#%d)", bank.getID());
					}

					plugin.getDatabase().getBanksAndAccounts(showConsoleMessages, Callback.of(plugin, map -> {

								for (Bank bank : map.keySet()) {
									addBank(bank, false);
									reloadedBanks.add(bank);
									for (Account account : map.get(bank)) {
										if (account.create(showConsoleMessages)) {
											accountUtils.addAccount(account, false, account.callUpdateName());
											reloadedAccounts.add(account);
										} else
											plugin.debug("Could not re-create account from database! (#" + account.getID() + ")");
									}
								}

								if (banks.size() != reloadedBanks.size())
									plugin.debugf("Number of banks before load was %d and is now %d.",
											banks.size(), reloadedBanks.size());
								if (accounts.size() != reloadedAccounts.size())
									plugin.debugf("Number of accounts before load was %d and is now %d",
											accounts.size(), reloadedAccounts.size());

								if (callback != null)
									callback.callSyncResult(new ReloadResult(reloadedBanks, reloadedAccounts));
							},
							callback::callSyncError
					));
				},
				callback::callSyncError
		));
	}

	public static class ReloadResult extends Pair<Collection<Bank>, Collection<Account>> {
		public ReloadResult(Collection<Bank> banks, Collection<Account> accounts) {
			super(banks, accounts);
		}
		public Collection<Bank> getBanks() { return super.getFirst(); }
		public Collection<Account> getAccounts() { return super.getSecond(); }
	}

	/**
	 * Calculates Gini coefficient of this bank. This is a measurement of wealth
	 * inequality among all n accounts at the bank.
	 *
	 * @return G = ( 2 * (sum(i...n) i * n[i].getBalance()) / n * (sum(i...n) n[i].getBalance()) ) - ( n + 1 / n )
	 */
	public static double getGiniCoefficient(Bank bank) {
		if (bank.getAccounts().isEmpty())
			return 0;
		List<BigDecimal> orderedValues = bank.getBalancesByOwner()
				.values()
				.stream()
				.sorted(BigDecimal::compareTo)
				.collect(Collectors.toList());
		BigDecimal valueSum = BigDecimal.ZERO;
		BigDecimal weightedValueSum = BigDecimal.ZERO;
		for (int i = 0; i < orderedValues.size(); i++) {
			valueSum = valueSum.add(orderedValues.get(i));
			weightedValueSum = weightedValueSum.add(orderedValues.get(i).multiply(BigDecimal.valueOf(i + 1)));
		}
		valueSum = valueSum.multiply(BigDecimal.valueOf(orderedValues.size()));
		weightedValueSum = weightedValueSum.multiply(BigDecimal.valueOf(2));
		if (valueSum.signum() == 0)
			return 0;
		BigDecimal leftSide = weightedValueSum.divide(valueSum, 10, RoundingMode.HALF_EVEN);
		BigDecimal rightSide = BigDecimal.valueOf((orderedValues.size() + 1) / orderedValues.size());
		return leftSide.subtract(rightSide).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
	}
}
