package com.monst.bankingplugin;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
import com.monst.bankingplugin.utils.Utils;

public class Account {

	private final BankingPlugin plugin;
	private boolean created;

	private int id;
	private String nickname;
	private final OfflinePlayer owner;
	private Set<OfflinePlayer> coowners;
	private final Location location;
	private final Bank bank;
	private InventoryHolder inventoryHolder;
	
	private final AccountStatus status;

	public Account(BankingPlugin plugin, OfflinePlayer owner, Bank bank, Location loc) {
		this(-1, plugin, owner, bank, loc, new AccountStatus(), null);
	}
	
	public Account(int id, BankingPlugin plugin, OfflinePlayer owner, Bank bank, Location loc) {
		this(id, plugin, owner, bank, loc, new AccountStatus(), null);
	}
	
	public Account(int id, BankingPlugin plugin, OfflinePlayer owner, Bank bank, Location loc, AccountStatus status) {
		this(id, plugin, owner, bank, loc, status, null);
	}
	
	public Account(int id, BankingPlugin plugin, OfflinePlayer owner, Bank bank, Location loc, AccountStatus status, String nickname) {
		this.id = id;
		this.plugin = plugin;
		this.owner = owner;
		this.bank = bank;
		this.location = loc;
		this.status = status;
		this.nickname = nickname;
		this.coowners = new HashSet<>();
	}

	public boolean create(boolean showConsoleMessages) {
		if (created) {
			plugin.debug("Account was already created! (#" + id + ")");
			return false;
		}

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

	public boolean isCreated() {
		return created;
	}

	public void clearNickname() {
		setNickname(null);
	}

	public void setDefaultNickname() {
		if (!hasID())
			return;
		setNickname(ChatColor.DARK_GREEN + owner.getName() + "'s Account " + ChatColor.GRAY + "(#" + id + ")");
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
		if (nickname == null)
			nickname = "";
		if (isDoubleChest()) {
			DoubleChest dc = (DoubleChest) inventoryHolder;
			if (dc == null)
				return;
			Chest left = (Chest) dc.getLeftSide();
			Chest right = (Chest) dc.getRightSide();
			left.setCustomName(Utils.colorize(nickname));
			left.update();
			right.setCustomName(Utils.colorize(nickname));
			right.update();
		} else {
			Chest chest = (Chest) inventoryHolder;
			if (chest == null)
				return;
			chest.setCustomName(Utils.colorize(nickname));
			chest.update();
		}
	}

	public String getNickname() {
		return nickname;
	}

	public boolean hasNickname() {
		return nickname != null;
	}

	public AccountStatus getStatus() {
		return status;
	}
	
	public Bank getBank() {
		return bank;
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

	public boolean isOwner(OfflinePlayer player) {
		return owner.getUniqueId().equals(player.getUniqueId());
	}

	public OfflinePlayer getOwner() {
		return owner;
	}

	public boolean isTrustedPlayerOnline() {
		return owner.isOnline() || coowners.stream().anyMatch(p -> p.isOnline());
	}

	public boolean isTrusted(OfflinePlayer p) {
		return p != null ? isOwner(p) || isCoowner(p) : false;
	}

	public boolean isCoowner(OfflinePlayer p) {
		return p != null ? coowners.contains(p) : false;
	}

	public void trustPlayer(OfflinePlayer p) {
		coowners.add(p);
	}

	public OfflinePlayer untrustPlayer(OfflinePlayer p) {
		if (coowners.contains(p)) {
			coowners.remove(p);
			return p;
		} else
			return null;
	}

	public Set<OfflinePlayer> getTrustedPlayersCopy() {
		Set<OfflinePlayer> trustedPlayers = new HashSet<>();
		trustedPlayers.add(owner);
		trustedPlayers.addAll(coowners);
		return Collections.unmodifiableSet(trustedPlayers);
	}

	public Set<OfflinePlayer> getCoownersCopy() {
		return Collections.unmodifiableSet(coowners);
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
	
	public short getChestSize() {
		return isDoubleChest() ? (short) 2 : 1;
	}

	public boolean isDoubleChest() {
		return inventoryHolder instanceof DoubleChest;
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
		String name;
		name = hasNickname() ? ChatColor.GRAY + "\"" + Utils.colorize(nickname) + ChatColor.GRAY + "\""
				: ChatColor.GRAY + "Account ID: " + ChatColor.WHITE + id;
		return name + "\n" + ChatColor.GRAY + "Owner: " + ChatColor.GOLD + owner.getName() + "\n" + ChatColor.GRAY
				+ "Bank: " + ChatColor.AQUA + bank.getName() + ChatColor.GRAY;
	}

	public String toStringVerbose() {
		String balance = "$" + Utils.formatNumber(status.getBalance());
		String multiplier = status.getRealMultiplier() + "x (Stage " + (status.getMultiplierStage() + 1) + " of "
				+ Config.interestMultipliers.size() + ")";
		String interestRate = "" + ChatColor.GREEN + (Config.baselineInterestRate * status.getRealMultiplier() * 100)
				+ "%" + ChatColor.GRAY + " (" + Config.baselineInterestRate + " x " + status.getRealMultiplier() + ")";
		if (status.getRemainingUntilFirstPayout() != 0)
			interestRate = interestRate
					.concat(ChatColor.RED + " (" + status.getRemainingUntilFirstPayout() + " payouts to go)");
		String loc = location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
		return toString() + "\n" + "Balance: " + ChatColor.GREEN + balance + "\n" + ChatColor.GRAY + "Multiplier: "
				+ multiplier + "\n" + "Interest rate: " + interestRate + "\n" + ChatColor.GRAY + "Location: " + loc
				+ "\n";
	}

	public enum TransactionType {
		DEPOSIT, WITHDRAWAL
	}

}
