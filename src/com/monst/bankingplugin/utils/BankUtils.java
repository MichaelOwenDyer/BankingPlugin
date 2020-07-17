package com.monst.bankingplugin.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.selections.CuboidSelection;
import com.monst.bankingplugin.selections.Selection;

public class BankUtils {

	private final Map<Selection, Bank> bankSelectionMap = new ConcurrentHashMap<>();
	private final Collection<Bank> locatedBanks = Collections.unmodifiableCollection(bankSelectionMap.values());
    private final BankingPlugin plugin;

    public BankUtils(BankingPlugin plugin) {
        this.plugin = plugin;
    }

    /**
	 * Get the bank at a given location
	 *
	 * @param location Location of the bank
	 * @return Bank at the given location or <b>null</b> if no bank is found there
	 */
	public Bank getBank(Location location) {
		for (Selection sel : bankSelectionMap.keySet())
			if (sel.contains(location))
				return bankSelectionMap.get(sel);
		return null;
    }

	/**
	 * Get the bank in a given selection
	 *
	 * @param protectedRegion Region of the bank
	 * @return Bank in the given region or <b>null</b> if no bank is found there
	 */
	public Bank getBank(Selection selection) {
		return bankSelectionMap.get(selection);
	}

    /**
	 * Checks whether there is a bank at a given location
	 * 
	 * @param location Location to check
	 * @return Whether there is a bank at the given location
	 */
	public boolean isBank(Location location) {
		return getBank(location) != null;
    }

    /**
	 * Get all banks Do not use for removing while iterating!
	 *
	 * @see #getBanksCopy()
	 * @return Read-only collection of all banks, may contain duplicates
	 */
	public Collection<Bank> getBanks() {
		return locatedBanks;
    }

    /**
	 * Get all banks Same as {@link #getBanks()} but this is safe to remove while
	 * iterating
	 *
	 * @see #getBanks()
	 * @return Copy of collection of all banks, may contain duplicates
	 */
	public Collection<Bank> getBanksCopy() {
		return new ArrayList<>(getBanks());
    }

	public Collection<Bank> getPlayerBanksCopy(OfflinePlayer owner) {
		return getBanksCopy().stream().filter(bank -> bank.isOwner(owner))
				.collect(Collectors.toSet());
	}

	public boolean isUniqueName(String name) {
		return getBanksCopy().stream().noneMatch(bank -> bank.getName().equalsIgnoreCase(name));
	}

	public boolean isExclusiveSelection(Selection sel) {
		return isExclusiveSelectionWithoutThis(sel, null);
	}

	public boolean isExclusiveSelectionWithoutThis(Selection sel, Bank bank) {

		Set<Selection> selections = new HashSet<>(bankSelectionMap.keySet());
		Optional.ofNullable(bank).ifPresent(b -> selections.remove(bank.getSelection()));
		for (Selection existingSel : selections)
			if (existingSel.overlaps(sel))
				return false;
		return true;
	}

	public void resizeBank(Bank bank, Selection newSel) {
		bankSelectionMap.remove(bank.getSelection());
		bankSelectionMap.put(newSel, bank);
		bank.setSelection(newSel);
	}
	
	public Selection parseCoordinates(String[] args, Location loc) throws NumberFormatException {

		if (args.length == 5 || args.length == 6) {

			String argX = args[2];
			String argY = args[3];
			String argZ = args[4];

			int x1, x2, y1, y2, z1, z2;

			x1 = argX.startsWith("~") ? Integer.parseInt(argX.substring(1, argX.length())) : Integer.parseInt(argX);
			y1 = argY.startsWith("~") ? Integer.parseInt(argY.substring(1, argY.length())) : Integer.parseInt(argY);
			z1 = argZ.startsWith("~") ? Integer.parseInt(argZ.substring(1, argZ.length())) : Integer.parseInt(argZ);

			x2 = loc.getBlockX();
			y2 = loc.getBlockY();
			z2 = loc.getBlockZ();

			if (argX.startsWith("~"))
				x1 += x2;
			if (argY.startsWith("~"))
				y1 += y2;
			if (argZ.startsWith("~"))
				z1 += z2;

			Location loc1 = new Location(loc.getWorld(), x1, y1, z1);
			Location loc2 = new Location(loc.getWorld(), x2, y2, z2);
			return new CuboidSelection(loc.getWorld(), loc1, loc2);

		} else if (args.length == 8 || args.length == 9) {

			String argX1 = args[2];
			String argY1 = args[3];
			String argZ1 = args[4];
			String argX2 = args[5];
			String argY2 = args[6];
			String argZ2 = args[7];

			int x1, y1, z1, x2, y2, z2;

			x1 = argX1.startsWith("~") ? Integer.parseInt(argX1.substring(1, argX1.length())) : Integer.parseInt(argX1);
			y1 = argY1.startsWith("~") ? Integer.parseInt(argY1.substring(1, argY1.length())) : Integer.parseInt(argY1);
			z1 = argZ1.startsWith("~") ? Integer.parseInt(argZ1.substring(1, argZ1.length())) : Integer.parseInt(argZ1);
			x2 = argX2.startsWith("~") ? Integer.parseInt(argX2.substring(1, argX2.length())) : Integer.parseInt(argX2);
			y2 = argY2.startsWith("~") ? Integer.parseInt(argY2.substring(1, argY2.length())) : Integer.parseInt(argY2);
			z2 = argZ2.startsWith("~") ? Integer.parseInt(argZ2.substring(1, argZ2.length())) : Integer.parseInt(argZ2);

			if (argX1.startsWith("~"))
				x1 += loc.getBlockX();
			if (argY1.startsWith("~"))
				y1 += loc.getBlockY();
			if (argZ1.startsWith("~"))
				z1 += loc.getBlockZ();
			if (argX2.startsWith("~"))
				x2 += loc.getBlockX();
			if (argY2.startsWith("~"))
				y2 += loc.getBlockY();
			if (argZ2.startsWith("~"))
				z2 += loc.getBlockZ();

			Location loc1 = new Location(loc.getWorld(), x1, y1, z1);
			Location loc2 = new Location(loc.getWorld(), x2, y2, z2);
			return new CuboidSelection(loc.getWorld(), loc1, loc2);

		} else
			return null;
	}

	/**
	 * Determines whether a new bank selection still contains all accounts of that
	 * bank
	 * 
	 * @param bank Bank that is being resized
	 * @param sel  New selection for the bank
	 * @return Whether the new selection contains all accounts
	 */
	public boolean containsAllAccounts(Bank bank, Selection sel) {
		return bank.getAccounts().stream().allMatch(account -> sel.contains(account.getLocation()));
	}

    /**
	 * Add a bank
	 * 
	 * @param bank          Bank to add
	 * @param addToDatabase Whether the bank should also be added to the database
	 * @param callback      Callback that - if succeeded - returns the ID the bank
	 *                      had or was given (as {@code int})
	 */
	public void addBank(Bank bank, boolean addToDatabase, Callback<Integer> callback) {
		plugin.debug("Adding bank... (#" + bank.getID() + ")");

		bankSelectionMap.put(bank.getSelection(), bank);

        if (addToDatabase) {
			plugin.getDatabase().addBank(bank, callback);
        } else {
			if (callback != null)
				callback.callSyncResult(bank.getID());
        }

    }

    /**
	 * Add a bank
	 * 
	 * @param bank          Bank to add
	 * @param addToDatabase Whether the bank should also be added to the database
	 */
	public void addBank(Bank bank, boolean addToDatabase) {
		addBank(bank, addToDatabase, null);
    }

	/**
	 * Remove a bank. May not work properly if double chest doesn't exist!
	 * 
	 * @param bank               Bank to remove
	 * @param removeFromDatabase Whether the bank should also be removed from the
	 *                           database
	 * @param callback           Callback that - if succeeded - returns null
	 * @see BankUtils#removeBankById(int, boolean, Callback)
	 */
	public void removeBank(Bank bank, boolean removeFromDatabase, Callback<Void> callback) {
		plugin.debug("Removing bank (#" + bank.getID() + ")");

		bankSelectionMap.remove(bank.getSelection());

        if (removeFromDatabase) {
			plugin.getDatabase().removeBank(bank, callback);
        } else {
            if (callback != null) callback.callSyncResult(null);
        }
    }

    /**
	 * Remove an bank. May not work properly if double chest doesn't exist!
	 * 
	 * @param bank               Bank to remove
	 * @param removeFromDatabase Whether the bank should also be removed from the
	 *                           database
	 * @see BankUtils#removeBankById(int, boolean)
	 */
	public void removeBank(Bank bank, boolean removeFromDatabase) {
		removeBank(bank, removeFromDatabase, null);
    }

	public int removeBank(Collection<Bank> banks, boolean removeFromDatabase) {
		int count = banks.size();

		for (Bank bank : banks) {
			for (Account account : bank.getAccounts())
				plugin.getAccountUtils().removeAccount(account, removeFromDatabase);
			removeBank(bank, removeFromDatabase);
		}
		return count;
	}

	/**
	 * Get the bank limits of a player
	 * 
	 * @param player Player, whose bank limits should be returned
	 * @return The bank limits of the given player
	 */
	public int getBankLimit(Player player) {
		int limit = 0;
		boolean useDefault = true;

		for (PermissionAttachmentInfo permInfo : player.getEffectivePermissions()) {
			if (permInfo.getPermission().startsWith("bankingplugin.bank.limit.")
					&& player.hasPermission(permInfo.getPermission())) {
				if (permInfo.getPermission().equalsIgnoreCase(Permissions.BANK_NO_LIMIT)) {
					limit = -1;
					useDefault = false;
					break;
				} else {
					String[] spl = permInfo.getPermission().split("bankingplugin.bank.limit.");

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
		return (useDefault ? Config.defaultBankLimit : limit);
	}
	
	public int getVolumeLimit(Player player) {
		int limit = 0;
		boolean useDefault = true;

		for (PermissionAttachmentInfo permInfo : player.getEffectivePermissions()) {
			if (permInfo.getPermission().startsWith("bankingplugin.bank.size.")
					&& player.hasPermission(permInfo.getPermission())) {
				if (permInfo.getPermission().equalsIgnoreCase(Permissions.BANK_NO_SIZE_LIMIT)) {
					limit = -1;
					useDefault = false;
					break;
				} else {
					String[] spl = permInfo.getPermission().split("bankingplugin.bank.size.");

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
		return (useDefault ? Config.defaultBankVolumeLimit : limit);
	}

	/**
	 * Get the number of accounts owned by a certain player
	 * 
	 * @param player Player whose accounts should be counted
	 * @return The number of accounts owned by the player
	 */
	public int getNumberOfBanks(OfflinePlayer owner) {
		return Math.round(getPlayerBanksCopy(owner).stream().count());
	}

    /**
	 * Reload the plugin
	 * 
	 * @param reloadConfig        Whether the configuration should also be reloaded
	 * @param showConsoleMessages Whether messages about the language file should be
	 *                            shown in the console
	 * @param callback            Callback that - if succeeded - returns the amount
	 *                            of accounts that were reloaded (as {@code int})
	 */
	public void reload(boolean reloadConfig, final boolean showConsoleMessages, final Callback<int[]> callback) {
		plugin.debug("Loading banks and accounts from database...");

		AccountUtils accountUtils = plugin.getAccountUtils();

        if (reloadConfig) {
			plugin.getPluginConfig().reload();
        }

		plugin.getDatabase().connect(new Callback<int[]>(plugin) {
            @Override
			public void onResult(int[] result) {
            	Collection<Bank> banks = getBanksCopy();
            	Collection<Account> accounts = accountUtils.getAccountsCopy();
            	
            	int[] preReload = { banks.size(), accounts.size() };
				int[] postReload = new int[2];
            	
				for (Bank bank : banks) {
					for (Account account : bank.getAccountsCopy()) {
						accountUtils.removeAccount(account, false);
						plugin.debug("Removed account (#" + account.getID() + ")");
					}
					removeBank(bank, false);
					plugin.debug("Removed bank (#" + bank.getID() + ")");
				}
				
				plugin.getDatabase().getBanksAndAccounts(showConsoleMessages, new Callback<Map<Bank, Collection<Account>>>(plugin) {
					@Override
					public void onResult(Map<Bank, Collection<Account>> result) {

						for (Bank bank : result.keySet()) {
							if (bank.create(showConsoleMessages)) {
								addBank(bank, false);
								postReload[0]++;
								for (Account account : result.get(bank)) {
									if (account.create(showConsoleMessages)) {
										accountUtils.addAccount(account, false, new Callback<Integer>(plugin) {
											@Override
											public void onResult(Integer result) {
												account.setNickname(account.getRawNickname());
											}
										});
										postReload[1]++;
									} else
										plugin.debug("Could not re-create account from database! (#" + account.getID() + ")");
								}
							} else
								plugin.debug("Could not re-create bank \"" + bank.getName() + "\" from database! (#" + bank.getID() + ")");
						}

						if (preReload[0] != postReload[0])
							plugin.debug("Number of banks before load was " + preReload[0] + ", and is now "
									+ postReload[0]);
						if (preReload[1] != postReload[1])
							plugin.debug("Number of accounts before load was " + preReload[1]
									+ ", and is now " + postReload[1]);
						
						if (callback != null)
							callback.callSyncResult(postReload);
					}
					
					@Override
					public void onError(Throwable throwable) {
						if (callback != null)
							callback.callSyncError(throwable);
					}
				});
            }

            @Override
            public void onError(Throwable throwable) {
				if (callback != null)
					callback.callSyncError(throwable);
            }
        });
    }

	public Bank getBankByName(String name) {
		return getBanksCopy().stream().filter(bank -> bank.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	public Bank getBankByID(int id) {
		return getBanksCopy().stream().filter(bank -> bank.getID() == id).findFirst().orElse(null);
	}
	
	public Bank lookupBank(String string) {
		Bank bank = null;
		try {
			int id = Integer.parseInt(string);
			bank = getBankByID(id);
		} catch (NumberFormatException e) {}
		if (bank == null)
			bank = getBankByName(string);
		return bank;
	}
}
