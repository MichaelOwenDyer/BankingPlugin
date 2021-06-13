package com.monst.bankingplugin.repository;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.banking.bank.BankField;
import com.monst.bankingplugin.geo.locations.ChestLocation;
import com.monst.bankingplugin.geo.selections.Selection;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.InterestEventScheduler;
import com.monst.bankingplugin.utils.Observable;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.block.Block;

import java.util.*;

public class BankRepository extends Observable implements Repository<Bank, BankField> {

	private final BankingPlugin plugin;
	private final Map<Selection, Bank> bankSelectionMap = new HashMap<>();

    public BankRepository(BankingPlugin plugin) {
        this.plugin = plugin;
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
	 * Gets the {@link Bank} around a given {@link ChestLocation}
	 *
	 * @param chestLocation a {@link ChestLocation} that is contained entirely by the bank
	 * @return Bank surrounding the given ChestLocation or <b>null</b> if no bank is found there
	 */
    @Override
    public Bank getAt(ChestLocation chestLocation) {
		for (Map.Entry<Selection, Bank> entry : bankSelectionMap.entrySet())
			if (entry.getKey().contains(chestLocation))
				return entry.getValue();
		return null;
	}

    /**
	 * Gets the {@link Bank} at a given block
	 *
	 * @param block a {@link Block} inside the bank
	 * @return Bank at the given block or <b>null</b> if no bank is found there
	 */
    @Override
	public Bank getAt(Block block) {
		for (Map.Entry<Selection, Bank> entry : bankSelectionMap.entrySet())
			if (entry.getKey().contains(block))
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
		InterestEventScheduler.scheduleAll(bank);

        if (addToDatabase)
			plugin.getDatabase().addBank(bank, callback);
        else
			Callback.yield(callback, bank.getID());

    }

    @Override
	public void update(Bank bank, Callback<Void> callback, BankField... fieldArray) {
		if (fieldArray.length == 0)
			return;
		EnumSet<BankField> fields = EnumSet.noneOf(BankField.class);
		fields.addAll(Arrays.asList(fieldArray));

		if (fields.remove(BankField.SELECTION)) {
			fields.add(BankField.WORLD);
			fields.add(BankField.MIN_X);
			fields.add(BankField.MAX_X);
			fields.add(BankField.MIN_Y);
			fields.add(BankField.MAX_Y);
			fields.add(BankField.MIN_Z);
			fields.add(BankField.MAX_Z);
			fields.add(BankField.VERTICES);
			bankSelectionMap.put(bank.getSelection(), bank);
		}
		plugin.debugf("Updating the following fields of bank #%d in the database: " + fields, bank.getID());

		plugin.getDatabase().updateBank(bank, fields, callback);

		notifyObservers();
		bank.notifyAccountObservers();
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

		InterestEventScheduler.unscheduleAll(bank);

        if (removeFromDatabase)
			plugin.getDatabase().removeBank(bank, callback);
        else
			Callback.yield(callback);
    }

	public Set<Selection> getOverlappingSelections(Selection sel) {
		return Utils.filter(bankSelectionMap.keySet(), s -> s.overlaps(sel));
	}

}
