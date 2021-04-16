package com.monst.bankingplugin.repository;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.geo.locations.ChestLocation;
import com.monst.bankingplugin.geo.selections.Selection;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.Observable;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.Location;

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

    @Override
    public Bank getAt(ChestLocation chestLocation) {
		for (Map.Entry<Selection, Bank> entry : bankSelectionMap.entrySet())
			if (entry.getKey().contains(chestLocation))
				return entry.getValue();
		return null;
	}

    /**
	 * Gets the {@link Bank} at a given location
	 *
	 * @param location {@link Location} of the bank
	 * @return Bank at the given location or <b>null</b> if no bank is found there
	 */
	public Bank getAt(Location location) {
		for (Map.Entry<Selection, Bank> entry : bankSelectionMap.entrySet())
			if (entry.getKey().contains(location))
				return entry.getValue();
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
        else
			Callback.yield(callback, bank.getID());

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
		plugin.debugf("Removing bank #%d", bank.getID());

		bank.getAccounts().forEach(account -> plugin.getAccountRepository().remove(account, removeFromDatabase));

		bankSelectionMap.remove(bank.getSelection());
		plugin.getBankRepository().notifyObservers();

		plugin.getScheduler().unschedulePayouts(bank);

        if (removeFromDatabase)
			plugin.getDatabase().removeBank(bank, callback);
        else
			Callback.yield(callback);
    }

	public Set<Selection> getOverlappingSelections(Selection sel) {
		return Utils.filter(bankSelectionMap.keySet(), s -> s.overlaps(sel));
	}

}
