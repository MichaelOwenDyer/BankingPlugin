package com.monst.bankingplugin.utils;

import com.earth2me.essentials.Essentials;
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
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class AccountUtils {

	private final BankingPlugin plugin;
	private final Map<Location, Account> accountLocationMap = new ConcurrentHashMap<>();

    public AccountUtils(BankingPlugin plugin) {
        this.plugin = plugin;
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

    public Account getAccount(int id) {
		return getAccountsCopy(account -> account.getID() == id).stream().findFirst().orElse(null);
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
     * Gets all accounts on the server.
     * Do not use for removing while iterating!
     *
     * @see #getAccountsCopy()
     * @return Read-only collection of all accounts
     */
    public Set<Account> getAccounts() {
		return new HashSet<>(accountLocationMap.values());
    }

    /**
     * Gets all accounts on the server.
     * Does the same thing as {@link #getAccounts()} but this is safe to remove while iterating.
     *
     * @see #getAccounts()
     * @return Copy of collection of all accounts, may contain duplicates
     */
    public Set<Account> getAccountsCopy() {
		return Collections.unmodifiableSet(getAccounts());
    }

    public Set<Account> getAccountsCopy(Predicate<? super Account> filter) {
		return Collections.unmodifiableSet(Utils.filter(getAccounts(), filter));
    }

	/**
	 * Get the number of accounts owned by a certain player
	 *
	 * @param player Player whose accounts should be counted
	 * @return The number of accounts owned by the player
	 */
	public int getNumberOfAccounts(OfflinePlayer player) {
		return getAccountsCopy(account -> account.isOwner(player)).size();
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
			if (callback != null)
				callback.callSyncResult(account.getID());
        }
    }

    /**
     * Adds and saves an account in the current session. Can also be used to update an already existing account.
     * @param account Account to add
     * @param addToDatabase Whether the account should also be added to or updated in the database
     */
    public void addAccount(Account account, boolean addToDatabase) {
        addAccount(account, addToDatabase, null);
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
        } else {
            if (callback != null) callback.callSyncResult(null);
        }
    }

    /**
     * Remove an account. May not work properly if double chest doesn't exist!
     * @param account Account to remove
     * @param removeFromDatabase Whether the account should also be removed from the database
	 */
    public void removeAccount(Account account, boolean removeFromDatabase) {
        removeAccount(account, removeFromDatabase, null);
    }

	public void removeAccounts(Collection<Account> accounts, boolean removeFromDatabase) {
		accounts.forEach(account -> removeAccount(account, removeFromDatabase));
	}

    /**
     * Get the account limits of a player
     * @param player Player, whose account limits should be returned
     * @return The account limits of the given player
     */
    public int getAccountLimit(Player player) {
        int limit = 0;
        boolean useDefault = true;

        for (PermissionAttachmentInfo permInfo : player.getEffectivePermissions()) {
			if (permInfo.getPermission().startsWith("bankingplugin.account.limit.")
					&& player.hasPermission(permInfo.getPermission())) {
                if (permInfo.getPermission().equalsIgnoreCase(Permissions.ACCOUNT_NO_LIMIT)) {
                    limit = -1;
                    useDefault = false;
                    break;
                } else {
					String[] spl = permInfo.getPermission().split("bankingplugin.account.limit.");

                    if (spl.length > 1) {
                        try {
                            int newLimit = Integer.parseInt(spl[1]);
                            if (newLimit < 0) {
                                limit = -1;
                                break;
                            }
                            limit = Math.max(limit, newLimit);
                            useDefault = false;
						} catch (NumberFormatException ignored) {}
                    }
                }
            }
        }
		if (limit < -1)
			limit = -1;
        return (useDefault ? Config.defaultAccountLimit : limit);
    }

	public BigDecimal appraiseAccountContents(Account account) {
		plugin.debug("Appraising account contents... (#" + account.getID() + ")");
		Essentials essentials = plugin.getEssentials();

		BigDecimal sum = BigDecimal.ZERO;
		for (ItemStack item : account.getInventory(true).getContents()) {
			if (item == null)
				continue;
			if (Config.blacklist.contains(item.getType().toString()))
				continue;
			BigDecimal itemValue = getWorth(essentials, item);
			if (item.getItemMeta() instanceof BlockStateMeta) {
				BlockStateMeta im = (BlockStateMeta) item.getItemMeta();
                if (im.getBlockState() instanceof ShulkerBox) {
                	ShulkerBox shulkerBox = (ShulkerBox) im.getBlockState();
                	for (ItemStack innerItem : shulkerBox.getInventory().getContents()) {
                		if (innerItem == null)
                			continue;
                		if (Config.blacklist.contains(innerItem.getType().toString()))
							continue;
						BigDecimal innerItemValue = getWorth(essentials, innerItem);
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
		plugin.debug("Appraised account balance: $" + Utils.format(sum) + " (#" + account.getID() + ")");
		return sum.setScale(2, RoundingMode.HALF_EVEN);
	}

	private BigDecimal getWorth(Essentials ess, ItemStack item) {
		BigDecimal worth = ess.getWorth().getPrice(ess, item);
		return worth != null ? worth : BigDecimal.ZERO;
	}

}
