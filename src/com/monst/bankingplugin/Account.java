package com.monst.bankingplugin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
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
import com.monst.bankingplugin.utils.AccountConfig.Field;
import com.monst.bankingplugin.utils.AccountStatus;
import com.monst.bankingplugin.utils.Ownable;
import com.monst.bankingplugin.utils.Utils;

import net.md_5.bungee.api.chat.TextComponent;

public class Account extends Ownable {

	private final BankingPlugin plugin;
	private boolean created;

	private String nickname;
	private final Location location;
	private final Bank bank;
	private InventoryHolder inventoryHolder;
	
	private final AccountStatus status;
	private BigDecimal balance;
	private BigDecimal prevBalance;

	public Account(BankingPlugin plugin, OfflinePlayer owner, Bank bank, Location loc) {
		this(-1, plugin, owner, null, bank, loc, new AccountStatus(bank.getAccountConfig()), null, BigDecimal.ZERO, BigDecimal.ZERO);
	}
	
	public Account(int id, BankingPlugin plugin, OfflinePlayer owner, Set<OfflinePlayer> coowners, Bank bank,
			Location loc, AccountStatus status, String nickname, BigDecimal balance, BigDecimal prevBalance) {
		this.id = id;
		this.plugin = plugin;
		this.owner = owner;
		this.coowners = coowners != null ? coowners : new HashSet<>();
		this.bank = bank;
		this.location = loc;
		this.status = status;
		this.nickname = nickname;
		this.balance = balance.setScale(2, RoundingMode.HALF_EVEN);
		this.prevBalance = prevBalance.setScale(2, RoundingMode.HALF_EVEN);
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
			
		} else if (!Utils.isTransparent(b.getRelative(BlockFace.UP))) {
			
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
			if (getBalance().signum() <= 0)
				plugin.debug("Cool! Account #" + id + " was created with a balance of " + Utils.formatNumber(checkedBalance)
						+ " already inside.");
			else
				plugin.debug("Value of account #" + id + " was found higher than expected. Expected: $"
						+ Utils.formatNumber(getBalance()) + " but was: $" + Utils.formatNumber(checkedBalance));
			setBalance(checkedBalance);
		} else if (checkedBalance.compareTo(getBalance()) == -1) {
			plugin.debug("Value of account #" + id + " was found lower than expected. Expected: $"
					+ Utils.formatNumber(getBalance()) + " but was: $" + Utils.formatNumber(checkedBalance));
			setBalance(checkedBalance);
		}

		created = true;
		return true;
	}

	public boolean isCreated() {
		return created;
	}

	public void clearNickname() {
		setNickname("");
	}

	public String getDefaultNickname() {
		return ChatColor.DARK_GREEN + owner.getName() + "'s Account " + ChatColor.GRAY + "(#" + id + ")";
	}

	public void setDefaultNickname() {
		if (!hasID())
			return;
		setNickname(null);
	}

	public void setNickname(String nickname) {
		if (nickname == null)
			nickname = getDefaultNickname();
		this.nickname = nickname;
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

	public BigDecimal getBalance() {
		return balance;
	}

	public BigDecimal getPrevBalance() {
		return prevBalance;
	}
	
	/**
	 * Saves the current balance of this account into the previous balance. Used
	 * only at interest payout events. Should only be used AFTER refreshing the
	 * account balance with AccountUtils.appraiseAccountContents() to ensure the
	 * balance is fully up-to-date.
	 */
	public void updatePrevBalance() {
		prevBalance = balance;
	}

	/**
	 * Changes the current balance of this account. Used every time the account
	 * chest is accessed and the contents are changed.
	 * 
	 * @param newBalance the new (positive) balance of the account.
	 */
	public void setBalance(BigDecimal newBalance) {
		if (newBalance != null && newBalance.signum() >= 0)
			this.balance = newBalance.setScale(2, RoundingMode.HALF_EVEN);
	}

	public Location getLocation() {
		return location;
	}

	private void updateInventory() throws ChestNotFoundException {
		Block b = location.getBlock();
		if (!(b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST))
			throw new ChestNotFoundException(String.format("No chest found in world '%s' at location: %d; %d; %d",
					b.getWorld().getName(), b.getX(), b.getY(), b.getZ()));
		Chest chest = (Chest) b.getState();
		inventoryHolder = chest.getInventory().getHolder();
	}

	public InventoryHolder getInventoryHolder() {
		try {
			updateInventory();
		} catch (ChestNotFoundException e) {
			plugin.debug(e);
			return null;
		}
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
		return "";
	}

	public TextComponent getInfo() {

		TextComponent info = new TextComponent();
		info.setColor(net.md_5.bungee.api.ChatColor.GRAY);

		info.addExtra(hasNickname() ? ChatColor.GRAY + "\"" + Utils.colorize(nickname) + ChatColor.GRAY + "\""
				: ChatColor.GRAY + "Account ID: " + ChatColor.AQUA + id);
		info.addExtra("\nOwner: " + ChatColor.GOLD + getOwnerDisplayName() + "\n");
		info.addExtra("Bank: " + ChatColor.AQUA + bank.getName());

		return info;
	}

	@SuppressWarnings("unchecked")
	public TextComponent getInfoVerbose() {
		TextComponent info = getInfo();
		info.setColor(net.md_5.bungee.api.ChatColor.GRAY);
		
		TextComponent message = new TextComponent("\nBalance: " + ChatColor.GREEN + "$" + Utils.formatNumber(getBalance()) + "\n");
		
		TextComponent multiplier = new TextComponent("Multiplier:");
		multiplier.addExtra(Utils.getMultiplierView((List<Integer>) bank.getAccountConfig().getOrDefault(Field.MULTIPLIERS),
						status.getMultiplierStage()));
		multiplier.addExtra("\nInterest rate: ");
		
		TextComponent interestRate = new TextComponent(ChatColor.GREEN + "" 
				+ Math.round(((double) bank.getAccountConfig().getOrDefault(Field.INTEREST_RATE) * status.getRealMultiplier() * 100))+ "%" 
				+ ChatColor.GRAY + " (" + bank.getAccountConfig().getOrDefault(Field.INTEREST_RATE) + " x "
				+ status.getRealMultiplier() + ")");
		
		if (status.getRemainingUntilFirstPayout() != 0)
			interestRate.addExtra(new TextComponent(
					ChatColor.RED + " (" + status.getRemainingUntilFirstPayout() + " payouts to go)"));
		
		TextComponent loc = new TextComponent(
				"\nLocation: " + ChatColor.AQUA + "(" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ")");
		
		info.addExtra(message);
		info.addExtra(multiplier);
		info.addExtra(interestRate);
		info.addExtra(loc);
		return info;
	}
}
