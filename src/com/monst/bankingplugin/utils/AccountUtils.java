package com.monst.bankingplugin.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;

import com.earth2me.essentials.Essentials;
import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.events.account.AccountRemoveAllEvent;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.EconomyResponse;

public class AccountUtils {

    private final Map<Location, Account> accountLocationMap = new ConcurrentHashMap<>();
    private final Collection<Account> locatedAccounts = Collections.unmodifiableCollection(accountLocationMap.values());
    private final BankingPlugin plugin;

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
        return locatedAccounts;
    }

    /**
     * Get all accounts
     * Same as {@link #getAccounts()} but this is safe to remove while iterating
     *
     * @see #getAccounts()
     * @return Copy of collection of all accounts, may contain duplicates
     */
    public Collection<Account> getAccountsCopy() {
		return locatedAccounts.stream().distinct().collect(Collectors.toUnmodifiableSet());
    }

	public Collection<Account> getPlayerAccountsCopy(OfflinePlayer owner) {
		return getAccounts().stream().filter(account -> account.getOwner().getUniqueId().equals(owner.getUniqueId()))
				.collect(Collectors.toList());
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
            Chest r = (Chest) dc.getRightSide();
            Chest l = (Chest) dc.getLeftSide();

            plugin.debug("Added account as double chest. (#" + account.getID() + ")");

            accountLocationMap.put(r.getLocation(), account);
            accountLocationMap.put(l.getLocation(), account);
        } else {
            plugin.debug("Added account as single chest. (#" + account.getID() + ")");

            accountLocationMap.put(account.getLocation(), account);
        }

        if (addToDatabase) {
			plugin.getDatabase().addAccount(account, callback);
        } else {
            if (callback != null) callback.callSyncResult(account.getID());
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
     * @see AccountUtils#removeAccountById(int, boolean, Callback)
     */
    public void removeAccount(Account account, boolean removeFromDatabase, Callback<Void> callback) {
        plugin.debug("Removing account (#" + account.getID() + ")");

        InventoryHolder ih = account.getInventoryHolder();

        if (ih instanceof DoubleChest) {
            DoubleChest dc = (DoubleChest) ih;
            Chest r = (Chest) dc.getRightSide();
            Chest l = (Chest) dc.getLeftSide();

            accountLocationMap.remove(r.getLocation());
            accountLocationMap.remove(l.getLocation());
        } else {
            accountLocationMap.remove(account.getLocation());
        }

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
     * @see AccountUtils#removeAccountById(int, boolean)
     */
    public void removeAccount(Account account, boolean removeFromDatabase) {
        removeAccount(account, removeFromDatabase, null);
    }

    /**
     * Remove a account by its ID
     * @param accountId ID of the account to remove
     * @param removeFromDatabase Whether the account should also be removed from the database
     * @param callback Callback that - if succeeded - returns null
     */
    public void removeAccountById(int accountId, boolean removeFromDatabase, Callback<Void> callback) {
        Map<Location, Account> toRemove = accountLocationMap.entrySet().stream()
                .filter(e -> e.getValue().getID() == accountId)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        plugin.debug(String.format("Removing %d account(s) with ID %d", toRemove.size(), accountId));

        if (toRemove.isEmpty()) {
            if (callback != null) callback.callSyncResult(null);
            return;
        }

        toRemove.forEach((loc, account) -> {
            accountLocationMap.remove(loc);
        });

        // Database#removeAccount removes account by ID so this only needs to be called once
        if (removeFromDatabase) {
			plugin.getDatabase().removeAccount(toRemove.values().iterator().next(), callback);
        } else {
            if (callback != null) callback.callSyncResult(null);
        }
    }

    /**
     * Remove a account by its ID
     * @param accountId ID of the account to remove
     * @param removeFromDatabase Whether the account should also be removed from the database
     */
    public void removeAccountById(int accountId, boolean removeFromDatabase) {
        removeAccountById(accountId, removeFromDatabase, null);
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
                            int newLimit = Integer.valueOf(spl[1]);
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
	public int getNumberOfAccounts(OfflinePlayer owner) {
		return (int) Math.round(getPlayerAccountsCopy(owner).stream()
				.mapToDouble(account -> account.getChestSize() == 1 ? 1.0 : 0.5).sum());
    }

	@SuppressWarnings("deprecation")
	public String getAccountList(CommandSender sender, String request, String[] args) {
		String list = "";
		int[] index = new int[1];
		if (sender instanceof Player) {
			Player p = (Player) sender;
			switch (request) {
			case "":
				list = getPlayerAccountsCopy(p).stream().distinct().map(Account::toString)
						.collect(Collectors.joining("\n", "" + ChatColor.GOLD + "" + index[0]++, ""));
				break;
			case "-d":
				list = getPlayerAccountsCopy(p).stream().distinct().map(Account::toStringVerbose)
						.collect(Collectors.joining("\n", "" + ChatColor.GOLD + "" + index[0]++, ""));
				break;
			case "-a":
				list = getAccountsCopy().stream().distinct().map(Account::toString)
						.collect(Collectors.joining("\n", "" + ChatColor.GOLD + "" + index[0]++, ""));
				break;
			case "-a -d":
				list = getAccountsCopy().stream().distinct().map(Account::toStringVerbose)
						.collect(Collectors.joining("\n", ChatColor.GOLD + "" + index[0]++, ""));
				break;
			case "name":
				OfflinePlayer ownerA = Bukkit.getOfflinePlayer(args[1]);
				list = getPlayerAccountsCopy(ownerA).stream().distinct().map(Account::toString)
						.collect(Collectors.joining("\n", ChatColor.GOLD + "" + index[0]++, ""));
				break;
			case "name -d":
				OfflinePlayer ownerB = Bukkit.getOfflinePlayer(args[1]);
				list = getPlayerAccountsCopy(ownerB).stream().distinct().map(Account::toString)
						.collect(Collectors.joining("\n", ChatColor.GOLD + "" + index[0]++, ""));
				break;
			default:
				return Messages.ERROR_OCCURRED;
			}
		} else {
			switch (request) {
			case "-a":
				list = getAccountsCopy().stream().map(Account::toString)
						.collect(Collectors.joining("\n", ChatColor.GOLD + "" + index[0]++, ""));
				break;
			case "-a -d":
				list = getAccountsCopy().stream().map(Account::toStringVerbose)
						.collect(Collectors.joining("\n", ChatColor.GOLD + "" + index[0]++, ""));
				break;
			case "name":
				OfflinePlayer ownerA = Bukkit.getOfflinePlayer(args[1]);
				list = getPlayerAccountsCopy(ownerA).stream().map(Account::toString)
						.collect(Collectors.joining("\n", ChatColor.GOLD + "" + index[0]++, ""));
				break;
			case "name -d":
				OfflinePlayer ownerB = Bukkit.getOfflinePlayer(args[1]);
				list = getPlayerAccountsCopy(ownerB).stream().map(Account::toString)
						.collect(Collectors.joining("\n", ChatColor.GOLD + "" + index[0]++, ""));
				break;
			default:
				return Messages.ERROR_OCCURRED;
			}
		}
		return list.equals("") ? Messages.NO_ACCOUNTS_FOUND : list;
	}

	@SuppressWarnings("deprecation")
	public List<Account> toRemoveList(CommandSender sender, String request, String[] args) {
		BankUtils bankUtils = plugin.getBankUtils();
		if (sender instanceof Player) {
			Player p = (Player) sender;
			switch (request) {
			case "":
				return new ArrayList<>(getPlayerAccountsCopy(p));
			case "-a":
				return new ArrayList<>(getAccountsCopy());
			case "name":
				OfflinePlayer owner = Bukkit.getOfflinePlayer(args[1]);
				return new ArrayList<>(getPlayerAccountsCopy(owner));
			case "bank":
				Bank bankA = bankUtils.lookupBank(args[2]);
				return getPlayerAccountsCopy(p).stream().filter(account -> account.getBank().equals(bankA))
						.collect(Collectors.toList());
			case "-a bank":
				Bank bankB = bankUtils.lookupBank(args[2]);
				return getAccountsCopy().stream().filter(account -> account.getBank().equals(bankB))
						.collect(Collectors.toList());
			default:
				return new LinkedList<>();
			}
		} else {
			switch (request) {
			case "-a":
				return new ArrayList<>(getAccountsCopy());
			case "name":
				OfflinePlayer owner = Bukkit.getOfflinePlayer(args[1]);
				return new ArrayList<>(getPlayerAccountsCopy(owner));
			case "-a bank":
				Bank bankB = bankUtils.lookupBank(args[2]);
				return getAccountsCopy().stream().filter(account -> account.getBank().equals(bankB))
						.collect(Collectors.toList());
			default:
				return new LinkedList<>();
			}
		}
	}

	@SuppressWarnings("deprecation")
	public int removeAll(CommandSender sender, String request, String[] args) {

		BankUtils bankUtils = plugin.getBankUtils();
		List<Account> affectedAccounts = toRemoveList(sender, request, args);

		AccountRemoveAllEvent event = new AccountRemoveAllEvent(sender, affectedAccounts);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			plugin.debug("Remove all event cancelled");
			return 0;
		}

		if (sender instanceof Player) {
			Player p = (Player) sender;
			switch (request) {
			case "":
				plugin.debug(p.getName() + " is removing all of their accounts");
				break;
			case "-a":
				plugin.debug(p.getName() + " is removing all accounts on the server");
				break;
			case "name":
				OfflinePlayer owner = Bukkit.getOfflinePlayer(args[1]);
				plugin.debug(p.getName() + " is removing all of " + owner.getName() + "'s accounts.");
				break;
			case "-b":
				Bank bankA = bankUtils.lookupBank(args[2]);
				plugin.debug(p.getName() + " is removing all of their accounts at " + bankA.getName());
				break;
			case "-a -b":
				Bank bankB = bankUtils.lookupBank(args[3]);
				plugin.debug(p.getName() + " is removing all accounts at " + bankB.getName());
				break;
			}
		} else {
			switch (request) {
			case "-a":
				plugin.debug(sender.getName() + " is removing all accounts on the server");
				break;
			case "name":
				OfflinePlayer owner = Bukkit.getOfflinePlayer(args[1]);
				plugin.debug(sender.getName() + " is removing all of " + owner.getName() + "'s accounts.");
				break;
			case "-a -b":
				Bank bankB = bankUtils.lookupBank(args[2]);
				plugin.debug(sender.getName() + " is removing all accounts at " + bankB.getName());
				break;
			}
		}

		affectedAccounts.forEach(account -> removeAccount(account, true));

		return affectedAccounts.size();
	}

	public BigDecimal appraiseAccountContents(Account account) {

		Essentials essentials = plugin.getEssentials();
		BigDecimal sum = BigDecimal.ZERO;
		sum.setScale(2, RoundingMode.HALF_EVEN);
		for (ItemStack items : account.getInventoryHolder().getInventory().getContents()) {
			if (items == null)
				continue;
			if (Config.blacklist.contains(items.getType().toString()))
				continue;
			BigDecimal value = essentials.getWorth().getPrice(essentials, items);
			if (value == null) {
				plugin.debug("An item without value (" + items.getType().toString() + ") was placed into account (#" + account.getID() + ")");
				continue;
			}
			sum = sum.add(value);
		}
		return sum.setScale(2, RoundingMode.HALF_EVEN);
	}

	public boolean payInsurance(Account account, BigDecimal loss) {
		long insurance = Config.insureAccountsUpTo;
		if (insurance == 0)
			return false;
		EconomyResponse response;
		if (insurance < 0)
			response = plugin.getEconomy().depositPlayer(account.getOwner(), loss.doubleValue());
		else {
			double payoutAmount = loss.doubleValue() > insurance ? insurance : loss.doubleValue();
			response = plugin.getEconomy().depositPlayer(account.getOwner(), payoutAmount);
		}
		return response.transactionSuccess();
	}
}
