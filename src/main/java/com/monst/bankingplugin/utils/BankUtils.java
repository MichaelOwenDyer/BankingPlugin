package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.selections.CuboidSelection;
import com.monst.bankingplugin.selections.Selection;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BankUtils {

	private final BankingPlugin plugin;
	private final Map<Selection, Bank> bankSelectionMap = new ConcurrentHashMap<>();

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
	 * @param selection Region of the bank
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
	 * @return Read-only collection of all banks
	 */
	public Set<Bank> getBanks() {
		return new HashSet<>(bankSelectionMap.values());
    }

    /**
	 * Get all banks Same as {@link #getBanks()} but this is safe to remove while
	 * iterating
	 *
	 * @see #getBanks()
	 * @return Copy of collection of all banks, may contain duplicates
	 */
	public Set<Bank> getBanksCopy() {
		return Collections.unmodifiableSet(getBanks());
    }

    public Set<Bank> getBanksCopy(Predicate<? super Bank> filter) {
		return Collections.unmodifiableSet(Utils.filter(getBanks(), filter));
	}

	public boolean isUniqueName(String name) {
		return isUniqueNameIgnoring(name, null);
	}

	public boolean isUniqueNameIgnoring(String name, String without) {
		if (without != null)
			return getBanksCopy(b -> !b.getName().contentEquals(without) && b.getName().contentEquals(name)).isEmpty();
		return getBanksCopy(b -> b.getName().contentEquals(name)).isEmpty();
	}

	public boolean isExclusiveSelection(Selection sel) {
		return isExclusiveSelectionIgnoring(sel, null);
	}

	public boolean isExclusiveSelectionIgnoring(Selection sel, Bank bank) {
		if (bank != null)
			return Utils.filter(bankSelectionMap.keySet(),
					s -> !bank.getSelection().equals(s) && bank.getSelection().overlaps(sel)).isEmpty();
		return Utils.filter(bankSelectionMap.keySet(), s -> s.overlaps(sel)).isEmpty();
	}

	public void resizeBank(Bank bank, Selection newSel, Callback<Integer> callback) {
		removeBank(bank, false);
		bank.setSelection(newSel);
		addBank(bank, true, callback);
	}

	/**
	 * Parses coordinates for a new bank selection from command arguments
	 * @param args the arguments to parse
	 * @param loc the location of the player sending the command
	 * @param offset how many extra arguments there are before the coordinates start
	 * @return a {@link CuboidSelection} described by the command arguments
	 * @throws NumberFormatException if the coordinates could not be parsed
	 */
	public CuboidSelection parseCoordinates(String[] args, Location loc, int offset) throws NumberFormatException {
		if (args.length >= 4 && args.length <= 6) {

			String argX = args[1 + offset];
			String argY = args[2 + offset];
			String argZ = args[3 + offset];

			int x1, y1, z1, x2, y2, z2;

			x2 = loc.getBlockX();
			y2 = loc.getBlockY();
			z2 = loc.getBlockZ();

			x1 = argX.startsWith("~") ? Integer.parseInt(argX.substring(1)) + x2 : Integer.parseInt(argX);
			y1 = argY.startsWith("~") ? Integer.parseInt(argY.substring(1)) + y2 : Integer.parseInt(argY);
			z1 = argZ.startsWith("~") ? Integer.parseInt(argZ.substring(1)) + z2 : Integer.parseInt(argZ);

			Location loc1 = new Location(loc.getWorld(), x1, y1, z1);
			Location loc2 = new Location(loc.getWorld(), x2, y2, z2);
			return CuboidSelection.of(loc.getWorld(), loc1, loc2);

		} else if (args.length >= 7 || args.length <= 9) {

			String argX1 = args[1 + offset];
			String argY1 = args[2 + offset];
			String argZ1 = args[3 + offset];
			String argX2 = args[4 + offset];
			String argY2 = args[5 + offset];
			String argZ2 = args[6 + offset];

			int x1, y1, z1, x2, y2, z2;

			x1 = argX1.startsWith("~") ? Integer.parseInt(argX1.substring(1)) + loc.getBlockX() : Integer.parseInt(argX1);
			y1 = argY1.startsWith("~") ? Integer.parseInt(argY1.substring(1)) + loc.getBlockY() : Integer.parseInt(argY1);
			z1 = argZ1.startsWith("~") ? Integer.parseInt(argZ1.substring(1)) + loc.getBlockZ() : Integer.parseInt(argZ1);
			x2 = argX2.startsWith("~") ? Integer.parseInt(argX2.substring(1)) + loc.getBlockX() : Integer.parseInt(argX2);
			y2 = argY2.startsWith("~") ? Integer.parseInt(argY2.substring(1)) + loc.getBlockY() : Integer.parseInt(argY2);
			z2 = argZ2.startsWith("~") ? Integer.parseInt(argZ2.substring(1)) + loc.getBlockZ() : Integer.parseInt(argZ2);

			Location loc1 = new Location(loc.getWorld(), x1, y1, z1);
			Location loc2 = new Location(loc.getWorld(), x2, y2, z2);
			return CuboidSelection.of(loc.getWorld(), loc1, loc2);

		} else
			return null;
	}

	/**
	 * Determines whether a new bank selection still encompasses all the accounts of that bank
	 * 
	 * @param bank Bank that is being resized
	 * @param sel  New selection for the bank
	 * @return Whether the new selection contains all accounts
	 */
	public boolean containsAllAccounts(Bank bank, Selection sel) {
		return bank.getAccounts().stream().map(Account::getLocation).allMatch(sel::contains);
	}

    /**
	 * Adds a bank
	 * 
	 * @param bank          Bank to add
	 * @param addToDatabase Whether the bank should also be added to the database
	 * @param callback      Callback that - if succeeded - returns the ID the bank
	 *                      had or was given (as {@code int})
	 */
	public void addBank(Bank bank, boolean addToDatabase, Callback<Integer> callback) {
		plugin.debug("Adding/updating bank... (#" + bank.getID() + ")");

		bankSelectionMap.put(bank.getSelection(), bank);

        if (addToDatabase)
			plugin.getDatabase().addBank(bank, callback);
        else if (callback != null)
				callback.callSyncResult(bank.getID());

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
	 */
	public void removeBank(Bank bank, boolean removeFromDatabase, Callback<Void> callback) {
		plugin.debug("Removing bank (#" + bank.getID() + ")");

		bankSelectionMap.remove(bank.getSelection());

        if (removeFromDatabase)
			plugin.getDatabase().removeBank(bank, callback);
        else if (callback != null)
            	callback.callSyncResult(null);
    }

    /**
	 * Remove an bank. May not work properly if double chest doesn't exist!
	 * 
	 * @param bank               Bank to remove
	 * @param removeFromDatabase Whether the bank should also be removed from the
	 *                           database
	 */
	public void removeBank(Bank bank, boolean removeFromDatabase) {
		removeBank(bank, removeFromDatabase, null);
    }

	public void removeBanks(Collection<Bank> banks, boolean removeFromDatabase) {
		for (Bank bank : banks) {
			for (Account account : bank.getAccountsCopy())
				plugin.getAccountUtils().removeAccount(account, removeFromDatabase);
			removeBank(bank, removeFromDatabase);
		}
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
		return useDefault ? Config.defaultBankLimit : limit;
	}
	
	public long getVolumeLimit(Player player) {
		long limit = 0;
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
							long newLimit = Long.parseLong(spl[1]);
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
		return useDefault ? Config.maximumBankVolume : limit;
	}

	/**
	 * Get the number of accounts owned by a certain player
	 * 
	 * @param player Player whose accounts should be counted
	 * @return The number of accounts owned by the player
	 */
	public int getNumberOfBanks(OfflinePlayer player) {
		return getBanksCopy(b -> b.isOwner(player)).size();
	}

	public static class ReloadResult extends Pair<Collection<Bank>, Collection<Account>> {
		public ReloadResult(Collection<Bank> banks, Collection<Account> accounts) {
			super(banks, accounts);
		}
		public Collection<Bank> getBanks() {
			return super.getFirst();
		}
		public Collection<Account> getAccounts() {
			return super.getSecond();
		}
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
	public void reload(boolean reloadConfig, boolean showConsoleMessages, Callback<ReloadResult> callback) {
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
            	
				Set<Bank> loadedBanks = new HashSet<>();
				Set<Account> loadedAccounts = new HashSet<>();
            	
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
							addBank(bank, false);
							loadedBanks.add(bank);
							for (Account account : result.get(bank)) {
								if (account.create(showConsoleMessages)) {
									accountUtils.addAccount(account, false);
									loadedAccounts.add(account);
								} else
									plugin.debug("Could not re-create account from database! (#" + account.getID() + ")");
							}
						}

						if (banks.size() != loadedBanks.size())
							plugin.debug(String.format("Number of banks before load was %d and is now %d.",
									banks.size(), loadedBanks.size()));
						if (accounts.size() != loadedAccounts.size())
							plugin.debug(String.format("Number of accounts before load was %d and is now %d",
									accounts.size(), loadedAccounts.size()));
						
						if (callback != null)
							callback.callSyncResult(new ReloadResult(loadedBanks, loadedAccounts));
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

	public Bank lookupBank(String identifier) {
		try {
			int id = Integer.parseInt(identifier);
			return getBanksCopy().stream().filter(b -> b.getID() == id).findFirst().orElse(null);
		} catch (NumberFormatException ignored) {}
		return getBanksCopy().stream().filter(b -> b.getName().equalsIgnoreCase(identifier)).findFirst().orElse(null);
	}

	/**
	 * Calculates Gini coefficient of this bank. This is a measurement of wealth
	 * inequality among all n accounts at the bank.
	 *
	 * @return G = ( 2 * sum(i,n)(i * value of ith account) / n * sum(i,n)(value of
	 *         ith account) ) - ( n + 1 / n )
	 */
	public static double getGiniCoefficient(Bank bank) {
		if (bank.getAccounts().isEmpty())
			return 0;
		List<BigDecimal> orderedValues = bank.getBalancesByOwner()
				.values()
				.stream()
				.sorted(BigDecimal::compareTo)
				.collect(Collectors.toList());
		BigDecimal valueSum = BigDecimal.ZERO;
		BigDecimal weightedValueSum = BigDecimal.ZERO;
		for (int i = 0; i < orderedValues.size(); i++) {
			valueSum = valueSum.add(orderedValues.get(i));
			weightedValueSum = weightedValueSum.add(orderedValues.get(i).multiply(BigDecimal.valueOf(i + 1)));
		}
		valueSum = valueSum.multiply(BigDecimal.valueOf(orderedValues.size()));
		weightedValueSum = weightedValueSum.multiply(BigDecimal.valueOf(2));
		if (valueSum.signum() == 0)
			return 0;
		BigDecimal leftEq = weightedValueSum.divide(valueSum, 10, RoundingMode.HALF_EVEN);
		BigDecimal rightEq = BigDecimal.valueOf((orderedValues.size() + 1) / orderedValues.size());
		BigDecimal gini = leftEq.subtract(rightEq).setScale(2, RoundingMode.HALF_EVEN);
		return gini.doubleValue();
	}

	public static String getEqualityLore(Bank bank) {
		double gini = 1 - getGiniCoefficient(bank);
		ChatColor color;
		String assessment = "";
		switch ((int) (gini * 5)) {
			case 0:
				color = ChatColor.DARK_RED;
				assessment = "(Very Poor)";
				break;
			case 1:
				color = ChatColor.RED;
				assessment = "(Poor)";
				break;
			case 2:
				color = ChatColor.YELLOW;
				assessment = "(Good)";
				break;
			case 3:
				color = ChatColor.GREEN;
				assessment = "(Very Good)";
				break;
			case 4: case 5:
				color = ChatColor.DARK_GREEN;
				assessment = "(Excellent)";
				break;
			default:
				color = ChatColor.GRAY;
		}
		return "" + color + Math.round(gini * 100) + "% " + assessment;
	}
}
