package com.monst.bankingplugin.utils;

import com.earth2me.essentials.Essentials;
import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
	public Account getAccount(Location location) { // XXX
		if (location == null)
			return null;
		return accountLocationMap.get(Utils.blockifyLocation(location));
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
     * Get all accounts
     * Do not use for removing while iterating!
     *
     * @see #getAccountsCopy()
     * @return Read-only collection of all accounts, may contain duplicates
     */
    public Collection<Account> getAccounts() {
		return new HashSet<>(accountLocationMap.values());
    }

    /**
     * Get all accounts
     * Same as {@link #getAccounts()} but this is safe to remove while iterating
     *
     * @see #getAccounts()
     * @return Copy of collection of all accounts, may contain duplicates
     */
    public Collection<Account> getAccountsCopy() {
		return Collections.unmodifiableCollection(getAccounts());
    }

	public Collection<Account> getPlayerAccountsCopy(OfflinePlayer owner) {
		return getAccounts().stream().filter(account -> account.isOwner(owner))
				.collect(Collectors.toList());
    }

	public Collection<Account> getBankAccountsCopy(Bank bank) {
		return getAccounts().stream().filter(account -> account.getBank().equals(bank)).collect(Collectors.toList());
	}

    /**
     * Add a account
     * @param account Account to add
     * @param addToDatabase Whether the account should also be added to the database
     * @param callback Callback that - if succeeded - returns the ID the account had or was given (as {@code int})
     */
    public void addAccount(Account account, boolean addToDatabase, Callback<Integer> callback) {
        InventoryHolder ih = account.getInventoryHolder();
        plugin.debug("Adding account... (#" + account.getID() + ")");

        if (ih instanceof DoubleChest) {
			DoubleChest dc = (DoubleChest) ih;
			Chest l = (Chest) dc.getLeftSide();
			Chest r = (Chest) dc.getRightSide();

			plugin.debug("Added account as double chest. (#" + account.getID() + ")");

			accountLocationMap.put(Utils.blockifyLocation(r.getLocation()), account);
			accountLocationMap.put(Utils.blockifyLocation(l.getLocation()), account);
        } else {
            plugin.debug("Added account as single chest. (#" + account.getID() + ")");

			accountLocationMap.put(Utils.blockifyLocation(account.getLocation()), account);
        }

        if (addToDatabase) {
			plugin.getDatabase().addAccount(account, callback);
        } else {
			account.getBank().addAccount(account);
			if (callback != null)
				callback.callSyncResult(account.getID());
        }
    }

    /**
     * Add a account
     * @param account Account to add
     * @param addToDatabase Whether the account should also be added to the database
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

		account.clearNickname();

        InventoryHolder ih = account.getInventoryHolder();

        if (ih instanceof DoubleChest) {
            DoubleChest dc = (DoubleChest) ih;
			Chest r = (Chest) dc.getRightSide();
			Chest l = (Chest) dc.getLeftSide();

			accountLocationMap.remove(Utils.blockifyLocation(r.getLocation()));
			accountLocationMap.remove(Utils.blockifyLocation(l.getLocation()));
        } else {
			accountLocationMap.remove(Utils.blockifyLocation(account.getLocation()));
        }

        if (removeFromDatabase) {
			plugin.getDatabase().removeAccount(account, callback);
			account.getBank().removeAccount(account);
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

	public void removeAccount(Collection<Account> accounts, boolean removeFromDatabase) {
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
						} catch (NumberFormatException e) {
						}
                    }
                }
            }
        }
		if (limit < -1)
			limit = -1;
        return (useDefault ? Config.defaultAccountLimit : limit);
    }

    /**
	 * Get the number of accounts owned by a certain player
	 * 
	 * @param player Player whose accounts should be counted
	 * @return The number of accounts owned by the player
	 */
	public int getNumberOfAccounts(OfflinePlayer player) {
		return (int) Math.round(getPlayerAccountsCopy(player).stream()
				.mapToDouble(account -> account.getChestSize() == 1 ? 1.0 : 0.5).sum());
    }

	public BigDecimal appraiseAccountContents(Account account) {

		plugin.debug("Appraising account contents... (#" + account.getID() + ")");

		BigDecimal sum = BigDecimal.ZERO;
		for (ItemStack item : account.getInventoryHolder().getInventory().getContents()) {
			if (item == null)
				continue;
			if (Config.blacklist.contains(item.getType().toString()))
				continue;
			BigDecimal itemValue = getWorth(item);

			if (item.getItemMeta() instanceof BlockStateMeta) {
				BlockStateMeta im = (BlockStateMeta) item.getItemMeta();
                if (im.getBlockState() instanceof ShulkerBox) {
                	ShulkerBox shulkerBox = (ShulkerBox) im.getBlockState();
                	for (ItemStack innerItems : shulkerBox.getInventory().getContents()) {
                		if (innerItems == null)
                			continue;
                		if (Config.blacklist.contains(innerItems.getType().toString()))
							continue;
						BigDecimal innerItemValue = getWorth(innerItems);
						if (innerItemValue.signum() == 1)
            				innerItemValue = innerItemValue.multiply(BigDecimal.valueOf(innerItems.getAmount()));
            			else {
							plugin.debug("An item without value (" + item.getType().toString()
									+ ") was placed into account (#" + account.getID() + ")");
            				continue;
            			}
						itemValue = itemValue.add(innerItemValue);
                	}
                }
			}

			if (itemValue.signum() == 1)
				itemValue = itemValue.multiply(BigDecimal.valueOf(item.getAmount()));
			else {
				plugin.debug("An item without value (" + item.getType().toString() + ") was placed into account (#" + account.getID() + ")");
				continue;
			}
			sum = sum.add(itemValue);
		}
		plugin.debug("Appraised account balance: " + sum + " (#" + account.getID() + ")");
		return sum.setScale(2, RoundingMode.HALF_EVEN);
	}

	private BigDecimal getWorth(ItemStack items) {
		Essentials essentials = plugin.getEssentials();
		return Optional.ofNullable(essentials.getWorth().getPrice(essentials, items)).orElse(BigDecimal.ZERO);
	}

}
