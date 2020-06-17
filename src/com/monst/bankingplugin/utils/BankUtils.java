package com.monst.bankingplugin.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.selections.CuboidSelection;
import com.monst.bankingplugin.selections.Polygonal2DSelection;
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

	public boolean isUniqueName(String name) {
		return getBanksCopy().stream().noneMatch(bank -> bank.getName().equalsIgnoreCase(name));
	}

	public boolean isAllowedName(String name) {
		return name.matches("[a-zA-Z]+");
	}

	public boolean isExclusiveSelection(Selection sel) {
		return isExclusiveSelectionWithoutThis(sel, null);
	}

	public boolean isExclusiveSelectionWithoutThis(Selection sel, Bank bank) {

		Set<Selection> selections = new HashSet<>(bankSelectionMap.keySet());
		Optional.ofNullable(bank).ifPresent(b -> selections.remove(bank.getSelection()));
		
		int minX = sel.getMinimumPoint().getBlockX();
		int maxX = sel.getMaximumPoint().getBlockX();
		int minY = sel.getMinimumPoint().getBlockY();
		int maxY = sel.getMaximumPoint().getBlockY();
		int minZ = sel.getMinimumPoint().getBlockZ();
		int maxZ = sel.getMaximumPoint().getBlockZ();
		for (Selection existing : selections) {
			if (overlaps(minX, maxX, existing.getMinimumPoint().getBlockX(), existing.getMaximumPoint().getBlockX())
					&& overlaps(minY, maxY, existing.getMinimumPoint().getBlockY(), existing.getMaximumPoint().getBlockY())
					&& overlaps(minZ, maxZ, existing.getMinimumPoint().getBlockZ(), existing.getMaximumPoint().getBlockZ()))
				return false;
		}
		return true;
	}

	private boolean overlaps(int rangeMinExisting, int rangeMaxExisting, int rangeMinNew, int rangeMaxNew) {
		return (rangeMinExisting >= rangeMinNew && rangeMinExisting <= rangeMaxNew)
				|| (rangeMaxExisting >= rangeMinNew && rangeMaxExisting <= rangeMaxNew)
				|| (rangeMinNew >= rangeMinExisting && rangeMinNew <= rangeMaxExisting)
				|| (rangeMaxNew >= rangeMinExisting && rangeMaxNew <= rangeMaxExisting);
	}

	public void resizeBank(Bank bank, Selection newSel) {
		bankSelectionMap.remove(bank.getSelection());
		bankSelectionMap.put(newSel, bank);
		bank.setSelection(newSel);
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
		return bank.getAccounts().stream().noneMatch(account -> !sel.contains(account.getLocation()));
	}

	public List<Location> getVertices(Selection sel) {
		List<Location> vertices = new LinkedList<>();
		World world = sel.getWorld();
		
		if (sel instanceof CuboidSelection) {
			CuboidSelection cuboidSel = (CuboidSelection) sel;
			
			int[] minmaxX = {cuboidSel.getMinimumPoint().getBlockX(), cuboidSel.getMaximumPoint().getBlockX()};
			int[] minmaxY = {cuboidSel.getMinimumPoint().getBlockY(), cuboidSel.getMaximumPoint().getBlockY()};
			int[] minmaxZ = {cuboidSel.getMinimumPoint().getBlockZ(), cuboidSel.getMaximumPoint().getBlockZ()};
			
			for (int x : minmaxX)
				for (int y : minmaxY)
					for (int z : minmaxZ)
						vertices.add(new Location(world,x,y,z));
			
		} else if (sel instanceof Polygonal2DSelection) {
			Polygonal2DSelection polySel = (Polygonal2DSelection) sel;
			
			int minY = polySel.getMinimumPoint().getBlockY();
			int maxY = polySel.getMaximumPoint().getBlockY();
			
			for (BlockVector2D bv : polySel.getNativePoints()) {
				vertices.add(new Location(world, bv.getBlockX(), minY, bv.getBlockZ()));
				vertices.add(new Location(world, bv.getBlockX(), maxY, bv.getBlockZ()));
			}
		}
		
		return vertices;
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

    /**
	 * Remove a bank by its ID
	 * 
	 * @param bankId             ID of the bank to remove
	 * @param removeFromDatabase Whether the bank should also be removed from the
	 *                           database
	 * @param callback           Callback that - if succeeded - returns null
	 */
	public void removeBankById(int bankId, boolean removeFromDatabase, Callback<Void> callback) {
		Map<Selection, Bank> toRemove = bankSelectionMap.entrySet().stream()
				.filter(e -> e.getValue().getID() == bankId)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		plugin.debug(String.format("Removing %d bank(s) with ID %d", toRemove.size(), bankId));

        if (toRemove.isEmpty()) {
			if (callback != null)
				callback.callSyncResult(null);
            return;
        }

		toRemove.forEach((sel, bank) -> {
			bankSelectionMap.remove(sel);
        });

		// Database#removeBank removes bank by ID so this only needs to be called
		// once
        if (removeFromDatabase) {
			plugin.getDatabase().removeBank(toRemove.values().iterator().next(), callback);
        } else {
			if (callback != null)
				callback.callSyncResult(null);
        }
    }

    /**
	 * Remove a bank by its ID
	 * 
	 * @param bankId             ID of the bank to remove
	 * @param removeFromDatabase Whether the bank should also be removed from the
	 *                           database
	 */
	public void removeBankById(int bankId, boolean removeFromDatabase) {
		removeBankById(bankId, removeFromDatabase, null);
    }

	public List<Bank> toRemoveList(String request, String[] args) {
		switch (request) {
		case "-a":
			return new ArrayList<>(getBanksCopy());
		default:
			return new ArrayList<>();
		}
	}

	public void removeAll(String request, String[] args) {
		// TODO: implement
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
			if (permInfo.getPermission().startsWith(
					"bankingplugin.bank.limit.")
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


    /**
	 * Reload the accounts
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
			plugin.getPluginConfig().reload(false, showConsoleMessages);
        }

		plugin.getDatabase().connect(new Callback<int[]>(plugin) {
            @Override
			public void onResult(int[] result) {
            	Collection<Bank> banks = getBanksCopy();
            	Collection<Account> accounts = accountUtils.getAccountsCopy();
            	
            	int[] preReload = { banks.size(), accounts.size() };
				int[] postReload = new int[2];
            	
				for (Account account : accounts) {
					accountUtils.removeAccount(account, false);
					plugin.debug("Removed account (#" + account.getID() + ")");
                }

				for (Bank bank : banks) {
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
												account.setNickname(account.getNickname());
											}
										});
										bank.addAccount(account);
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
