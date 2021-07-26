package com.monst.bankingplugin.repository;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Bank;
import com.monst.bankingplugin.banking.BankField;
import com.monst.bankingplugin.geo.locations.AccountLocation;
import com.monst.bankingplugin.geo.regions.BankRegion;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.InterestEventScheduler;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.block.Block;

import java.util.*;

public class BankRepository implements Repository<Bank, BankField> {

	public static class BankMap extends EntityMap<BankRegion, Bank> {
		@Override
		BankRegion getLocation(Bank bank) {
			return bank.getRegion();
		}
	}

	private final BankingPlugin plugin;
	private final BankMap bankMap = new BankMap();

	public BankMap getBankMap() {
		return bankMap;
	}

    public BankRepository(BankingPlugin plugin) {
        this.plugin = plugin;
    }

	/**
	 * Gets all banks on the server.
	 *
	 * @return a {@link Set} of all banks.
	 */
	@Override
	public Set<Bank> getAll() {
		return new HashSet<>(bankMap.values());
	}

	/**
	 * Gets the {@link Bank} around a given {@link AccountLocation}
	 *
	 * @param accountLocation a {@link AccountLocation} that is contained entirely by the bank
	 * @return Bank surrounding the given AccountLocation or <b>null</b> if no bank is found there
	 */
    @Override
    public Bank getAt(AccountLocation accountLocation) {
		for (Map.Entry<BankRegion, Bank> entry : bankMap.entrySet())
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
		for (Map.Entry<BankRegion, Bank> entry : bankMap.entrySet())
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
		return bankMap.get(region);
	}

    /**
	 * Adds a bank to this repository and returns the bank's ID to the {@link Callback}.
	 *
	 * @param bank          The bank to add
	 * @param addToDatabase Whether to also add the bank to the database
	 * @param callback      Callback that returns the ID of the bank
	 */
    @Override
	public void add(Bank bank, boolean addToDatabase, Callback<Integer> callback) {
		plugin.debug("Adding/updating bank... (#" + bank.getID() + ")");

		bankMap.put(bank);
		InterestEventScheduler.scheduleBank(bank);

        if (addToDatabase)
			plugin.getDatabase().addBank(bank, callback);
        else
			Callback.callResult(callback, bank.getID());
    }

	/**
	 * Updates the specified fields of the specified bank in the database, and returns {@code null} to the {@link Callback}.
	 * @param bank the entity to update in the database
	 * @param callback the callback that will be called after updating is completed.
	 * @param fieldArray the fields to update in the database
	 */
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
			bankMap.put(bank);
		}
		plugin.debugf("Updating the following fields of bank #%d in the database: " + fields, bank.getID());
		plugin.getDatabase().updateBank(bank, fields, callback);
		bank.notifyAccountObservers();
	}

	/**
	 * Removes a bank from the repository.
	 *
	 * @param bank               The bank to remove
	 * @param removeFromDatabase Whether to also remove the bank from the database
	 * @param callback           Callback that returns {@code null} after removal
	 */
	@Override
	public void remove(Bank bank, boolean removeFromDatabase, Callback<Void> callback) {
		plugin.debugf("Removing bank #%d", bank.getID());

		bankMap.remove(bank);
		// Accounts will be deleted from the database automatically if the bank is
		bank.getAccounts().forEach(account -> plugin.getAccountRepository().remove(account, false));
		InterestEventScheduler.unscheduleAll(bank);

        if (removeFromDatabase)
			plugin.getDatabase().removeBank(bank, callback);
        else
			Callback.callResult(callback);
    }

	/**
	 * Gets the bank regions which overlap with the specified region.
	 * @return a {@link Set} of {@link BankRegion}s which overlap with the specified region.
	 */
	public Set<BankRegion> getOverlappingRegions(BankRegion sel) {
		return Utils.filter(bankMap.keySet(), s -> s.overlaps(sel));
	}

}
