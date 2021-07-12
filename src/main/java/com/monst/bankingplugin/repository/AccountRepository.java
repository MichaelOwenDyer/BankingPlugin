package com.monst.bankingplugin.repository;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.banking.AccountField;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.geo.locations.AccountLocation;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.Observable;
import com.monst.bankingplugin.utils.QuickMath;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.math.BigDecimal;
import java.util.*;

public class AccountRepository extends Observable implements Repository<Account, AccountField> {

	private final BankingPlugin plugin;
	private final Map<AccountLocation, Account> accountLocationMap = new HashMap<>();
	private final Set<Account> notFoundAccounts = new HashSet<>();

    public AccountRepository(BankingPlugin plugin) {
        this.plugin = plugin;
    }

	/**
	 * Checks whether there is an account located at a specified block.
	 * @return whether an account is present.
	 */
	public boolean isAccount(Block block) {
    	if (!Utils.isChest(block))
    		return false;
    	for (AccountLocation chest : accountLocationMap.keySet())
    		if (chest.contains(block))
    			return true;
		return false;
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

    /**
     * Gets the account at a given {@link AccountLocation}
     *
     * @param accountLocation AccountLocation of the account
     * @return Account at the given AccountLocation or <b>null</b> if no account is found there
     */
    @Override
	public Account getAt(AccountLocation accountLocation) {
    	if (accountLocation == null)
    		return null;
    	return accountLocationMap.get(accountLocation);
    }

	/**
	 * Gets the {@link Account} at a given block
	 *
	 * @param block a {@link Block} that the account is located at
	 * @return Account at the given block or <b>null</b> if no account is found there
	 */
	@Override
	public Account getAt(Block block) {
		if (!Utils.isChest(block))
			return null;
		for (Map.Entry<AccountLocation, Account> entry : accountLocationMap.entrySet())
			if (entry.getKey().contains(block))
				return entry.getValue();
		return null;
	}

	/**
	 * Adds and saves an account in the current session. Can also be used to update an already existing account.
	 * @param account Account to add
	 * @param addToDatabase Whether the account should also be added to or updated in the database
     * @param callback Callback that - if succeeded - returns the ID the account had or was given (as {@code int})
     */
	@Override
    public void add(Account account, boolean addToDatabase, Callback<Integer> callback) {
    	InventoryHolder ih = account.getInventoryHolder(true);
    	if (ih == null) {
    		plugin.debugf("Could not add account #%d because its inventory could not be found!", account.getID());
			return;
		}

        plugin.debugf("Adding account #%d to the session...", account.getID());
		accountLocationMap.put(account.getLocation(), account);

        if (addToDatabase) {
			plugin.getDatabase().addAccount(account, callback);
        } else {
			account.getBank().addAccount(account); // Account is otherwise added to the bank in Database
			Callback.callResult(callback, account.getID());
        }
        notifyObservers();
    }

    @Override
    public void update(Account account, Callback<Void> callback, AccountField... fieldArray) {
		if (fieldArray.length == 0)
			return;
		EnumSet<AccountField> fields = EnumSet.noneOf(AccountField.class);
		fields.addAll(Arrays.asList(fieldArray));

		if (fields.remove(AccountField.LOCATION)) {
			fields.add(AccountField.WORLD);
			fields.add(AccountField.Y);
			fields.add(AccountField.X1);
			fields.add(AccountField.Z1);
			fields.add(AccountField.X2);
			fields.add(AccountField.Z2);
			accountLocationMap.put(account.getLocation(), account);
		}
		plugin.debugf("Updating the following fields of account #%d in the database: " + fields, account.getID());

		plugin.getDatabase().updateAccount(account, fields, callback);

		notifyObservers();
		account.notifyObservers();
		account.getBank().notifyObservers();
	}

    /**
	 * Removes an account.
     * @param account Account to remove
     * @param removeFromDatabase Whether the account should also be removed from the database
     * @param callback Callback that - if succeeded - returns null
     */
    @Override
    public void remove(Account account, boolean removeFromDatabase, Callback<Void> callback) {
        plugin.debugf("Removing account #%d.", account.getID());

		account.clearChestName();
		account.getBank().removeAccount(account);

		accountLocationMap.remove(account.getLocation());

        if (removeFromDatabase) {
			plugin.getDatabase().removeAccount(account, callback);
        } else
        	Callback.callResult(callback);
        notifyObservers();
    }

	public Set<Account> getNotFoundAccounts() {
		return new HashSet<>(notFoundAccounts);
	}

	public void addInvalidAccount(Account account) {
    	if (account == null)
    		return;
		notFoundAccounts.add(account);
    	notifyObservers();
	}

	public void removeInvalidAccount(Account account) {
		if (account == null)
			return;
		notFoundAccounts.remove(account);
		notifyObservers();
	}

	public BigDecimal appraise(ItemStack[] contents) {
		BigDecimal sum = BigDecimal.ZERO;
		for (ItemStack item : contents) {
			if (item == null)
				continue;
			if (Config.blacklist.get().contains(item.getType()))
				continue;
			BigDecimal itemValue = getWorth(item);
			if (item.getItemMeta() instanceof BlockStateMeta) {
				BlockStateMeta im = (BlockStateMeta) item.getItemMeta();
                if (im.getBlockState() instanceof ShulkerBox) {
                	ShulkerBox shulkerBox = (ShulkerBox) im.getBlockState();
                	for (ItemStack innerItem : shulkerBox.getInventory().getContents()) {
                		if (innerItem == null)
                			continue;
                		if (Config.blacklist.get().contains(innerItem.getType()))
							continue;
						BigDecimal innerItemValue = getWorth(innerItem);
						if (innerItemValue.signum() != 0)
            				innerItemValue = QuickMath.multiply(innerItemValue, innerItem.getAmount());
						itemValue = itemValue.add(innerItemValue);
                	}
                }
			}
			if (itemValue.signum() != 0)
				itemValue = QuickMath.multiply(itemValue, item.getAmount());
			sum = sum.add(itemValue);
		}
		return QuickMath.scale(sum);
	}

	private BigDecimal getWorth(ItemStack item) {
    	BigDecimal worth = plugin.getEssentials().getWorth().getPrice(plugin.getEssentials(), item);
		return worth != null ? worth : BigDecimal.ZERO;
	}

}
