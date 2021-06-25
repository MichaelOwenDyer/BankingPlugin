package com.monst.bankingplugin.repository;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.banking.BankField;
import com.monst.bankingplugin.geo.locations.AccountLocation;
import com.monst.bankingplugin.geo.regions.BankRegion;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.InterestEventScheduler;
import com.monst.bankingplugin.utils.Observable;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.block.Block;

import java.util.*;

public class BankRepository extends Observable implements Repository<Bank, BankField> {

	private final BankingPlugin plugin;
	private final Map<BankRegion, Bank> bankRegionMap = new HashMap<>();

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
		return new HashSet<>(bankRegionMap.values());
	}

	/**
	 * Gets the {@link Bank} around a given {@link AccountLocation}
	 *
	 * @param accountLocation a {@link AccountLocation} that is contained entirely by the bank
	 * @return Bank surrounding the given AccountLocation or <b>null</b> if no bank is found there
	 */
    @Override
    public Bank getAt(AccountLocation accountLocation) {
		for (Map.Entry<BankRegion, Bank> entry : bankRegionMap.entrySet())
			if (entry.getKey().contains(accountLocation))
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
		for (Map.Entry<BankRegion, Bank> entry : bankRegionMap.entrySet())
			if (entry.getKey().contains(block))
				return entry.getValue();
		return null;
    }

	/**
	 * Gets the {@link Bank} with a given region
	 *
	 * @param region {@link BankRegion} of the bank
	 * @return Bank in the given region or <b>null</b> if no bank is found there
	 */
	public Bank getAt(BankRegion region) {
		return bankRegionMap.get(region);
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

		bankRegionMap.put(bank.getRegion(), bank);
		InterestEventScheduler.scheduleBank(bank);

        if (addToDatabase)
			plugin.getDatabase().addBank(bank, callback);
        else
			Callback.callResult(callback, bank.getID());

    }

    @Override
	public void update(Bank bank, Callback<Void> callback, BankField... fieldArray) {
		if (fieldArray.length == 0)
			return;
		EnumSet<BankField> fields = EnumSet.noneOf(BankField.class);
		fields.addAll(Arrays.asList(fieldArray));

		if (fields.remove(BankField.REGION)) {
			fields.add(BankField.WORLD);
			fields.add(BankField.MIN_X);
			fields.add(BankField.MAX_X);
			fields.add(BankField.MIN_Y);
			fields.add(BankField.MAX_Y);
			fields.add(BankField.MIN_Z);
			fields.add(BankField.MAX_Z);
			fields.add(BankField.VERTICES);
			bankRegionMap.put(bank.getRegion(), bank);
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

		bankRegionMap.remove(bank.getRegion());
		plugin.getBankRepository().notifyObservers();

		InterestEventScheduler.unscheduleAll(bank);

        if (removeFromDatabase)
			plugin.getDatabase().removeBank(bank, callback);
        else
			Callback.callResult(callback);
    }

	public Set<BankRegion> getOverlappingRegions(BankRegion sel) {
		return Utils.filter(bankRegionMap.keySet(), s -> s.overlaps(sel));
	}

}
