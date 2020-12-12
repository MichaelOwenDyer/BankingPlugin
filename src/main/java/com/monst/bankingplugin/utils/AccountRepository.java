package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.config.Config;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class AccountRepository extends Observable {

	private final BankingPlugin plugin;
	private final Map<Location, Account> accountLocationMap = new HashMap<>();
	private final Set<Account> invalidAccounts = new HashSet<>();

    public AccountRepository(BankingPlugin plugin) {
        this.plugin = plugin;
    }

	/**
	 * Checks whether there is a account at a given location
	 * @param location Location to check
	 * @return Whether there is a account at the given location
	 */
	public boolean isAccount(Location location) {
		return getAccount(location) != null;
	}

    /**
     * Get the account at a given location
     *
     * @param location Location of the account
     * @return Account at the given location or <b>null</b> if no account is found there
     */
	public Account getAccount(Location location) {
		if (location == null)
			return null;
		return accountLocationMap.get(Utils.blockifyLocation(location));
    }

	/**
	 * Get the account with a given ID
	 *
	 * @param id ID of the account
	 * @return Account with the given ID or <b>null</b> if no account has that ID
	 */
    public Account getAccount(int id) {
		return getAccounts(account -> account.getID() == id).stream().findFirst().orElse(null);
	}

    /**
     * Gets all accounts on the server.
     * Do not use for removing while iterating!
     *
     * @see #getAccounts()
     * @return Read-only collection of all accounts
     */
    public Set<Account> getAccounts() {
		return new HashSet<>(accountLocationMap.values());
    }

    public Set<Account> getAccounts(Predicate<? super Account> filter) {
		return Utils.filter(getAccounts(), filter);
    }

	/**
	 * Get the number of accounts owned by a certain player
	 *
	 * @param player Player whose accounts should be counted
	 * @return The number of accounts owned by the player
	 */
	public int getNumberOfAccounts(OfflinePlayer player) {
		return getAccounts(account -> account.isOwner(player)).size();
	}

	/**
	 * Adds and saves an account in the current session. Can also be used to update an already existing account.
	 * @param account Account to add
	 * @param addToDatabase Whether the account should also be added to or updated in the database
	 */
	public void addAccount(Account account, boolean addToDatabase) {
		addAccount(account, addToDatabase, null);
	}

	/**
	 * Adds and saves an account in the current session. Can also be used to update an already existing account.
	 * @param account Account to add
	 * @param addToDatabase Whether the account should also be added to or updated in the database
     * @param callback Callback that - if succeeded - returns the ID the account had or was given (as {@code int})
     */
    public void addAccount(Account account, boolean addToDatabase, Callback<Integer> callback) {
    	Inventory inv = account.getInventory(true);
    	if (inv == null) {
    		plugin.debug("Could not add account! Inventory null (#" + account.getID() + ")");
			return;
		}

		InventoryHolder ih = inv.getHolder();
        plugin.debug("Adding account to session... (#" + account.getID() + ")");

        if (ih instanceof DoubleChest) {
			DoubleChest dc = (DoubleChest) ih;
			Chest l = (Chest) dc.getLeftSide();
			Chest r = (Chest) dc.getRightSide();

			plugin.debug("Added account to session as double chest. (#" + account.getID() + ")");

			accountLocationMap.put(Utils.blockifyLocation(r.getLocation()), account);
			accountLocationMap.put(Utils.blockifyLocation(l.getLocation()), account);
        } else {
            plugin.debug("Added account to session as single chest. (#" + account.getID() + ")");

			accountLocationMap.put(Utils.blockifyLocation(account.getLocation()), account);
        }

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
	 * Remove an account. May not work properly if double chest doesn't exist!
	 * @param account Account to remove
	 * @param removeFromDatabase Whether the account should also be removed from the database
	 */
	public void removeAccount(Account account, boolean removeFromDatabase) {
		removeAccount(account, removeFromDatabase, null);
	}

    /** Remove a account. May not work properly if double chest doesn't exist!
     * @param account Account to remove
     * @param removeFromDatabase Whether the account should also be removed from the database
     * @param callback Callback that - if succeeded - returns null
     */
    public void removeAccount(Account account, boolean removeFromDatabase, Callback<Void> callback) {
        plugin.debug("Removing account (#" + account.getID() + ")");

		account.clearChestName();
		account.getBank().removeAccount(account);

		Inventory inv = account.getInventory(true);
		if (inv != null) {
			InventoryHolder ih = inv.getHolder();
			if (ih instanceof DoubleChest) {
				DoubleChest dc = (DoubleChest) ih;
				Chest r = (Chest) dc.getRightSide();
				Chest l = (Chest) dc.getLeftSide();

				accountLocationMap.remove(Utils.blockifyLocation(r.getLocation()));
				accountLocationMap.remove(Utils.blockifyLocation(l.getLocation()));
			} else {
				accountLocationMap.remove(Utils.blockifyLocation(account.getLocation()));
			}
		} else
			plugin.debug("Could not remove account. Inventory null (#" + account.getID() + ")");

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

    /**
     * Get the account limits of a player
     * @param player Player, whose account limits should be returned
     * @return The account limits of the given player
     */
    public int getAccountLimit(Player player) {
    	return (int) Utils.getLimit(player, Permissions.ACCOUNT_NO_LIMIT,
                Config.defaultAccountLimit);
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
