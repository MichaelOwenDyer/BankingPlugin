package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.exceptions.AccountNotFoundException;
import com.monst.bankingplugin.geo.locations.ChestLocation;
import org.bukkit.Location;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AccountRepository extends Observable implements Repository<Account> {

	private final BankingPlugin plugin;
	private final Map<ChestLocation, Account> accountLocationMap = new HashMap<>();
	private final Set<Account> invalidAccounts = new HashSet<>();

    public AccountRepository(BankingPlugin plugin) {
        this.plugin = plugin;
    }

    @Deprecated
    public boolean isAccount(Location location) {
    	for (ChestLocation chest : accountLocationMap.keySet())
    		if (chest.contains(location))
    			return true;
		return false;
	}

	/**
	 * Checks whether there is a account at a given location
	 * @param chest Location to check
	 * @return Whether there is a account at the given location
	 */
	public boolean isAccount(ChestLocation chest) {
		try {
			getAt(chest);
			return true;
		} catch (AccountNotFoundException e) {
			return false;
		}
	}

	/**
	 * Gets all accounts on the server.
	 *
	 * @see #getAll()
	 * @return Read-only collection of all accounts
	 */
	@Override
	public Set<Account> getAll() {
		return new HashSet<>(accountLocationMap.values());
	}

	@Deprecated
	public Account getAt(Location location) {
		for (Map.Entry<ChestLocation, Account> entry : accountLocationMap.entrySet())
			if (entry.getKey().contains(location))
				return entry.getValue();
		return null;
	}

    /**
     * Gets the account at a given location
     *
     * @param location Location of the account
     * @return Account at the given location or <b>null</b> if no account is found there
     */
    @Override
	public Account getAt(ChestLocation location) throws AccountNotFoundException {
    	Account account;
		if (location == null || (account = accountLocationMap.get(location)) == null)
			throw new AccountNotFoundException(location);
		return account;
    }

	/**
	 * Adds and saves an account in the current session. Can also be used to update an already existing account.
	 * @param account Account to add
	 * @param addToDatabase Whether the account should also be added to or updated in the database
     * @param callback Callback that - if succeeded - returns the ID the account had or was given (as {@code int})
     */
	@Override
    public void add(Account account, boolean addToDatabase, Callback<Integer> callback) {
    	Inventory inv = account.getInventory(true);
    	if (inv == null) {
    		plugin.debug("Could not add account! Inventory null (#" + account.getID() + ")");
			return;
		}

        plugin.debug("Adding account to session... (#" + account.getID() + ")");
		accountLocationMap.put(account.getChestLocation(), account);

        if (addToDatabase) {
			plugin.getDatabase().addAccount(account, callback);
        } else {
			account.getBank().addAccount(account); // Account is otherwise added to the bank in Database
			account.getBank().notifyObservers();
			if (callback != null)
				callback.callSyncResult(account.getID());
        }
        notifyObservers();
    }

    /**
	 * Removes a account. May not work properly if double chest doesn't exist!
     * @param account Account to remove
     * @param removeFromDatabase Whether the account should also be removed from the database
     * @param callback Callback that - if succeeded - returns null
     */
    @Override
    public void remove(Account account, boolean removeFromDatabase, Callback<Void> callback) {
        plugin.debug("Removing account (#" + account.getID() + ")");

		account.clearChestName();
		account.getBank().removeAccount(account);

		accountLocationMap.remove(account.getChestLocation());

        if (removeFromDatabase) {
			plugin.getDatabase().removeAccount(account, callback);
        } else if (callback != null)
        	callback.callSyncResult(null);
        notifyObservers();
    }

	public Set<Account> getInvalidAccounts() {
		return new HashSet<>(invalidAccounts);
	}

	public void addInvalidAccount(Account account) {
    	if (account == null)
    		return;
		invalidAccounts.add(account);
    	notifyObservers();
	}

	public void removeInvalidAccount(Account account) {
		if (account == null)
			return;
		invalidAccounts.remove(account);
		notifyObservers();
	}

	public BigDecimal appraise(ItemStack[] contents) {
		BigDecimal sum = BigDecimal.ZERO;
		for (ItemStack item : contents) {
			if (item == null)
				continue;
			if (Config.blacklist.contains(item.getType().toString()))
				continue;
			BigDecimal itemValue = getWorth(item);
			if (item.getItemMeta() instanceof BlockStateMeta) {
				BlockStateMeta im = (BlockStateMeta) item.getItemMeta();
                if (im.getBlockState() instanceof ShulkerBox) {
                	ShulkerBox shulkerBox = (ShulkerBox) im.getBlockState();
                	for (ItemStack innerItem : shulkerBox.getInventory().getContents()) {
                		if (innerItem == null)
                			continue;
                		if (Config.blacklist.contains(innerItem.getType().toString()))
							continue;
						BigDecimal innerItemValue = getWorth(innerItem);
						if (innerItemValue.signum() != 0)
            				innerItemValue = innerItemValue.multiply(BigDecimal.valueOf(innerItem.getAmount()));
						itemValue = itemValue.add(innerItemValue);
                	}
                }
			}
			if (itemValue.signum() != 0)
				itemValue = itemValue.multiply(BigDecimal.valueOf(item.getAmount()));
			sum = sum.add(itemValue);
		}
		return sum.setScale(2, RoundingMode.HALF_EVEN);
	}

	private BigDecimal getWorth(ItemStack item) {
		return Utils.nonNull(plugin.getEssentials().getWorth().getPrice(plugin.getEssentials(), item), () -> BigDecimal.ZERO);
	}

}
