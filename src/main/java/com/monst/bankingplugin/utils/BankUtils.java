package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.selections.Selection;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

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
		for (Map.Entry<Selection, Bank> selectionBankPair : bankSelectionMap.entrySet())
			if (selectionBankPair.getKey().contains(location))
				return selectionBankPair.getValue();
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

	public Bank getBank(String identifier) {
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
	 * Get the number of banks owned by a certain player
	 *
	 * @param player Player whose banks should be counted
	 * @return The number of banks owned by the player
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
	 * Removes a bank.
	 *
	 * @param bank               Bank to remove
	 * @param removeFromDatabase Whether the bank should also be removed from the
	 *                           database
	 */
	public void removeBank(Bank bank, boolean removeFromDatabase) {
		removeBank(bank, removeFromDatabase, null);
	}

	/**
	 * Removes a bank.
	 *
	 * @param bank               Bank to remove
	 * @param removeFromDatabase Whether the bank should also be removed from the
	 *                           database
	 * @param callback           Callback that - if succeeded - returns null
	 */
	public void removeBank(Bank bank, boolean removeFromDatabase, Callback<Void> callback) {
		plugin.debug("Removing bank (#" + bank.getID() + ")");

		bank.getAccounts().forEach(account -> plugin.getAccountUtils().removeAccount(account, removeFromDatabase));

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
}
