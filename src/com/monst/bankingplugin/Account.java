package com.monst.bankingplugin;

import java.math.BigDecimal;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.InventoryHolder;

import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.exceptions.ChestNotFoundException;
import com.monst.bankingplugin.exceptions.NotEnoughSpaceException;
import com.monst.bankingplugin.utils.AccountStatus;
import com.monst.bankingplugin.utils.ItemUtils;

public class Account {

	private final BankingPlugin plugin;
	private boolean created;

	private int id;
	private final OfflinePlayer owner;
	private final Location location;
	private final Bank bank;
	private InventoryHolder inventoryHolder;
	
	private AccountStatus status;

	public Account(BankingPlugin plugin, OfflinePlayer owner, Bank bank, Location loc) {
		this(-1, plugin, owner, bank, loc, new AccountStatus());
	}
	
	public Account(int id, BankingPlugin plugin, OfflinePlayer owner, Bank bank, Location loc) {
		this(id, plugin, owner, bank, loc, new AccountStatus());
	}
	
	public Account(int id, BankingPlugin plugin, OfflinePlayer owner, Bank bank, Location loc, AccountStatus status) {
		this.id = id;
		this.plugin = plugin;
		this.owner = owner;
		this.bank = bank;
		this.location = loc;
		this.status = status;
	}

	public boolean create(boolean showConsoleMessages) {
		if (created)
			return false;

		plugin.debug("Creating account (#" + id + ")");

		Block b = location.getBlock();
		if (!(b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST)) {
			
			ChestNotFoundException e = new ChestNotFoundException(
					String.format("No Chest found in world '%s' at location: %d; %d; %d", b.getWorld().getName(),
							b.getX(), b.getY(), b.getZ()));
			
			plugin.getAccountUtils().removeAccount(this, Config.removeAccountOnError);
			
			if (showConsoleMessages)
				plugin.getLogger().severe(e.getMessage());
			
			plugin.debug("Failed to create account (#" + id + ")");
			plugin.debug(e);
			return false;
			
		} else if (!ItemUtils.isTransparent(b.getRelative(BlockFace.UP))) {
			
			NotEnoughSpaceException e = new NotEnoughSpaceException(
					String.format("No space above chest in world '%s' at location: %d; %d; %d", b.getWorld().getName(),
							b.getX(), b.getY(), b.getZ()));
			
			plugin.getAccountUtils().removeAccount(this, Config.removeAccountOnError);
			
			if (showConsoleMessages)
				plugin.getLogger().severe(e.getMessage());
			
			plugin.debug("Failed to create account (#" + id + ")");
			plugin.debug(e);
			return false;
		}

		Chest chest = (Chest) b.getState();
		inventoryHolder = chest.getInventory().getHolder();

		BigDecimal checkedBalance = plugin.getAccountUtils().appraiseAccountContents(this);
		if (checkedBalance.compareTo(getBalance()) == 1) {
			if (getBalance().signum() <= 0) {
				plugin.debug("Cool! Account #" + id + " was created with a balance of " + checkedBalance.toString()
						+ " already inside.");
			} else {
				plugin.debug("Value of account #" + id + " was found higher than expected. Expected: $"
						+ getBalance().toString() + " but was: $" + checkedBalance);
			}
			setBalance(checkedBalance);

		} else if (checkedBalance.compareTo(getBalance()) == -1) {
			plugin.debug("Unexpected account value (#" + id + ")! Expected: $" + getBalance().toString() + " but was: $"
					+ checkedBalance.toString());
			if (plugin.getAccountUtils().payInsurance(this, getBalance().subtract(checkedBalance))) {
				plugin.debug("Insurance has been paid to account owner (#" + id + ")");
				setBalance(checkedBalance);
			} else {
				if (Config.insureAccountsUpTo < 0) {
					plugin.debug("There was an error while paying $" + getBalance().subtract(checkedBalance).toString()
							+ " in insurance.");
				} else if (Config.insureAccountsUpTo > 0) {
					double loss = getBalance().subtract(checkedBalance).doubleValue();
					double insurance = loss > Config.insureAccountsUpTo ? Config.insureAccountsUpTo : loss;
					plugin.debug("There was an error while paying the maximum $" + insurance + " in insurance.");
				}
			}
		}

		created = true;
		return true;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Account otherAccount = (Account) o;
		return id != -1 && id == otherAccount.id;
	}

	@Override
	public int hashCode() {
		return id != -1 ? id : super.hashCode();
	}
	
	@Override
	public String toString() {
		return ChatColor.GRAY + "Unique ID: " + id + "\n"
				+ "Owner: " + owner.getName() + "\n"
				+ "Bank: " + bank.getName();
	}

	public String toStringVerbose() {
		String balance = "$" + status.getBalance().toString();
		String multiplier = status.getRealMultiplier() + " (Stage " + status.getMultiplierStage() + " of " + Config.interestMultipliers.size() + ")";
		String payingInterest = status.getRemainingUntilFirstPayout() == 0 ? "True" : "False (" + status.getRemainingUntilFirstPayout() + " payouts to go";
		String loc = location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
		String size = isDoubleChest() ? "Double" : "Single";
		return toString() + "\n"
				+ "Balance: " + balance + "\n"
				+ "Multiplier: " + multiplier + "\n"
				+ "Paying interest: " + payingInterest + "\n"
				+ "Location: " + loc + "\n"
				+ "Size: " + size;
	}

	public short getChestSize() {
		return isDoubleChest() ? (short) 2 : 1;
	}

	public boolean isDoubleChest() {
		return inventoryHolder instanceof DoubleChest;
	}

	public AccountStatus getStatus() {
		return status;
	}
	
	public Bank getBank() {
		return bank;
	}

	public boolean isCreated() {
		return created;
	}

	public boolean hasID() {
		return id != -1;
	}

	public int getID() {
		return id;
	}
	
	public void setID(int id) {
		if (this.id == -1) {
			this.id = id;
		}
	}

	public OfflinePlayer getOwner() {
		return owner;
	}

	public BigDecimal getBalance() {
		return status.getBalance();
	}
	
	public void setBalance(BigDecimal newBalance) {
		status.setBalance(newBalance);
	}

	public BigDecimal getPrevBalance() {
		return status.getPrevBalance();
	}

	public Location getLocation() {
		return location;
	}

	public InventoryHolder getInventoryHolder() {
		return inventoryHolder;
	}
	
	public enum TransactionType {
		DEPOSIT, WITHDRAWAL
	}

}
