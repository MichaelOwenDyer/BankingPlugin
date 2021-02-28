package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.geo.selections.Selection;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BankRepository extends Observable implements Repository<Bank> {

	private final BankingPlugin plugin;
	private final Map<Selection, Bank> bankSelectionMap = new HashMap<>();

    public BankRepository(BankingPlugin plugin) {
        this.plugin = plugin;
    }

    /**
	 * Gets the {@link Bank} at a given location
	 *
	 * @param location {@link Location} of the bank
	 * @return Bank at the given location or <b>null</b> if no bank is found there
	 */
    @Override
	public Bank getAt(Location location) {
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
	public Bank getAt(Selection selection) {
		return bankSelectionMap.get(selection);
	}

    /**
	 * Gets all banks on the server.
	 *
	 * @return A new {@link HashSet} containing all banks
	 */
    @Override
	public Set<Bank> getAll() {
		return new HashSet<>(bankSelectionMap.values());
    }

    /**
	 * Adds a bank to the repository.
	 *
	 * @param bank          Bank to add
	 * @param addToDatabase Whether the bank should also be added to the database
	 * @param callback      Callback that - if succeeded - returns the ID the bank
	 *                      had or was given (as {@code int})
	 */
    @Override
	public void add(Bank bank, boolean addToDatabase, Callback<Integer> callback) {
		plugin.debug("Adding/updating bank... (#" + bank.getID() + ")");

		bankSelectionMap.put(bank.getSelection(), bank);
		plugin.getScheduler().schedulePayouts(bank);

        if (addToDatabase)
			plugin.getDatabase().addBank(bank, callback);
        else if (callback != null)
				callback.callSyncResult(bank.getID());

    }

	/**
	 * Removes a bank from the repository.
	 *
	 * @param bank               Bank to remove
	 * @param removeFromDatabase Whether the bank should also be removed from the
	 *                           database
	 * @param callback           Callback that - if succeeded - returns null
	 */
	@Override
	public void remove(Bank bank, boolean removeFromDatabase, Callback<Void> callback) {
		plugin.debug("Removing bank (#" + bank.getID() + ")");

		bank.getAccounts().forEach(account -> plugin.getAccountRepository().remove(account, removeFromDatabase));

		bankSelectionMap.remove(bank.getSelection());
		plugin.getBankRepository().notifyObservers();

		plugin.getScheduler().unschedulePayouts(bank);

        if (removeFromDatabase)
			plugin.getDatabase().removeBank(bank, callback);
        else if (callback != null)
			callback.callSyncResult(null);
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
	@Override
	public int getLimit(Player player) {
		return (int) Utils.getLimit(player, Permissions.BANK_NO_LIMIT, Config.defaultBankLimit);
	}

	/**
	 * Gets the bank volume limit of a certain player, to see if the player is allowed to create a bank of a certain size.
	 */
	public static long getVolumeLimit(Player player) {
		return Utils.getLimit(player, Permissions.BANK_NO_SIZE_LIMIT, Config.maximumBankVolume);
	}
}
