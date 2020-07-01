package com.monst.bankingplugin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import com.monst.bankingplugin.selections.CuboidSelection;
import com.monst.bankingplugin.selections.Polygonal2DSelection;
import com.monst.bankingplugin.selections.Selection;
import com.monst.bankingplugin.selections.Selection.SelectionType;
import com.monst.bankingplugin.utils.AccountConfig;
import com.monst.bankingplugin.utils.AccountConfig.Field;
import com.monst.bankingplugin.utils.Ownable;
import com.monst.bankingplugin.utils.Utils;

import net.md_5.bungee.api.ChatColor;

public class Bank extends Ownable {
	
	public enum BankType {
		PLAYER, ADMIN
	}

	private final BankingPlugin plugin;
	private boolean created;
	
	private int id;
	private String name;
	private final World world;
	private Selection selection;
	private final AccountConfig accountConfig;
	private final Set<Account> accounts;
	private final BankType type;

	// New admin bank
	public Bank(BankingPlugin plugin, String name, Selection selection) {
		this(-1, plugin, name, null, null, selection, new AccountConfig(), BankType.ADMIN);
	}

	// Old admin bank
	public Bank(int id, BankingPlugin plugin, String name, Selection selection, AccountConfig config) {
		this(id, plugin, name, null, null, selection, config, BankType.ADMIN);
	}

	// New player bank
	public Bank(BankingPlugin plugin, String name, OfflinePlayer owner, Set<OfflinePlayer> coowners,
			Selection selection) {
		this(-1, plugin, name, owner, coowners, selection, new AccountConfig(), BankType.PLAYER);
	}
	
	// Old player bank
	public Bank(int id, BankingPlugin plugin, String name, OfflinePlayer owner, Set<OfflinePlayer> coowners,
			Selection selection, AccountConfig config) {
		this(id, plugin, name, owner, coowners, selection, config, BankType.PLAYER);
	}

	public Bank(int id, BankingPlugin plugin, String name, OfflinePlayer owner, Set<OfflinePlayer> coowners,
			Selection selection, AccountConfig accountConfig, BankType type) {
		this.id = id;
		this.plugin = plugin;
		this.owner = owner;
		this.coowners = coowners;
		this.name = name;
		this.world = selection.getWorld();
		this.selection = selection;
		this.accounts = new HashSet<>();
		this.accountConfig = accountConfig;
		this.type = type;
	}
	
	public boolean create(boolean showConsoleMessages) {
		if (created) {
			plugin.debug("Bank was already created! (#" + id + ")");
			return false;
		}
		
		plugin.debug("Creating bank (#" + id + ")");

		created = true;
		return true;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public boolean hasID() {
		return id != -1;
	}
	
	public int getID() {
		return id;
	}
	
	public void setID(int id) {
		if (this.id == -1)
			this.id = id;
	}
	
	public World getWorld() {
		return world;
	}

	public Selection getSelection() {
		return selection;
	}

	public void setSelection(Selection sel) {
		this.selection = sel;
	}

	public AccountConfig getAccountConfig() {
		return accountConfig;
	}

	public BankType getType() {
		return type;
	}

	public boolean isAdminBank() {
		return type == BankType.ADMIN;
	}

	public String getCoordinates() {
		if (selection.getType() == SelectionType.CUBOID) {
			CuboidSelection sel = (CuboidSelection) selection;
			Location min = sel.getMinimumPoint();
			Location max = sel.getMaximumPoint();
			return "(" + min.getBlockX() + ", " + min.getBlockY() + ", " + min.getBlockZ() + ") to (" + max.getBlockX()
					+ ", " + max.getBlockY() + ", " + max.getBlockZ() + ")";
		} else {
			Polygonal2DSelection sel = (Polygonal2DSelection) selection;
			int minY = sel.getMinimumPoint().getBlockY();
			int maxY = sel.getMaximumPoint().getBlockY();
			return sel.getNativePoints().stream().map(vec -> "(" + vec.getBlockX() + ", " + vec.getBlockZ() + ")")
					.collect(Collectors.joining(", ")) + " at Y = " + minY + " to " + maxY;
		}
	}

	public Collection<Account> getAccounts() {
		return accounts;
	}
	
	public void addAccount(Account account) {
		if (account != null)
			accounts.add(account);
	}
	
	public void addAccount(Collection<Account> newAccounts) {
		if (newAccounts != null)
			newAccounts.forEach(account -> addAccount(account));
	}

	public void removeAccount(Account account) {
		if (account != null)
			accounts.remove(account);
	}

	public void removeAccount(Collection<Account> accountsToRemove) {
		if (accountsToRemove != null)
			accountsToRemove.forEach(account -> removeAccount(account));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Bank otherBank = (Bank) o;
		return id != -1 && id == otherBank.id;
	}

	@Override
	public int hashCode() {
		return id != -1 ? id : super.hashCode();
	}

	public BigDecimal getTotalValue() {
		if (created)
			return accounts.stream().map(account -> account.getBalance()).reduce(BigDecimal.ZERO,
					(value, sum) -> sum.add(value)).setScale(2, RoundingMode.HALF_EVEN);
		else
			return BigDecimal.ZERO;
	}
	
	public IntStream getAccountIDs() {
		return accounts.stream().mapToInt(account -> account.getID());
	}
	
	@Override
	public String toString() {
		return ChatColor.GRAY + "\"" + ChatColor.GOLD + Utils.colorize(name) + ChatColor.GRAY + "\" (#" + id + ")\n"
				+ ChatColor.GRAY + "Owner: " + (isAdminBank() ? ChatColor.RED + "ADMIN" : getOwnerDisplayName()) + "\n"
				+ ChatColor.GRAY + "Interest rate: " + ChatColor.GREEN + Utils.formatNumber((double) accountConfig.getOrDefault(Field.INTEREST_RATE)) + "\n"
				+ ChatColor.GRAY + "Multipliers: " + ChatColor.AQUA + accountConfig.getOrDefault(Field.MULTIPLIERS) + "\n"
				+ ChatColor.GRAY + "Account creation price: " + ChatColor.GREEN + "$" + Utils.formatNumber((double) accountConfig.getOrDefault(Field.ACCOUNT_CREATION_PRICE));
	}
	
	public String toStringVerbose() {
		double minBalance = (double) accountConfig.getOrDefault(Field.MINIMUM_BALANCE);
		boolean showFee = minBalance != 0;
		return toString() + "\n"
				+ ChatColor.GRAY + "Offline payouts: " + ChatColor.AQUA + accountConfig.getOrDefault(Field.ALLOWED_OFFLINE_PAYOUTS) 
				+ ChatColor.GRAY + " (" + ChatColor.AQUA + accountConfig.getOrDefault(Field.ALLOWED_OFFLINE_PAYOUTS_BEFORE_MULTIPLIER_RESET) + ChatColor.GRAY + " before reset)\n"
				+ ChatColor.GRAY + "Initial payout delay: " + ChatColor.AQUA + accountConfig.getOrDefault(Field.INITIAL_INTEREST_DELAY) + "\n"
				+ ChatColor.GRAY + "Minimum balance: " + ChatColor.GREEN + "$" + Utils.formatNumber(minBalance)
				+ ChatColor.GRAY + (showFee ? " (" + ChatColor.RED + "$" + Utils.formatNumber((double) accountConfig.getOrDefault(Field.LOW_BALANCE_FEE)) + ChatColor.GRAY + " fee)" : "") + "\n"
				+ ChatColor.GRAY + "Current accounts: " + ChatColor.AQUA + accounts.size() + "\n"
				+ ChatColor.GRAY + "Total value: " + ChatColor.GREEN + "$" + Utils.formatNumber(getTotalValue()) + "\n"
				+ ChatColor.GRAY + "Selection type: " + selection.getType() + "\n"
				+ ChatColor.GRAY + "Location: " + ChatColor.AQUA + getCoordinates(); // Remove location and selection type?
		// Make this more useful for customers
	}

}
