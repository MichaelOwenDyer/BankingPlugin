package com.monst.bankingplugin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import com.monst.bankingplugin.utils.AccountConfig;
import com.monst.bankingplugin.utils.Ownable;
import com.monst.bankingplugin.utils.Utils;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;

import net.md_5.bungee.api.ChatColor;

public class Bank extends Ownable {
	
	private final BankingPlugin plugin;
	private boolean created;
	
	private int id;
	private String name;
	private final World world;
	private Selection selection;
	private final AccountConfig accountConfig;
	private final Set<Account> accounts;

	public Bank(BankingPlugin plugin, String name, OfflinePlayer owner, Set<OfflinePlayer> coowners,
			Selection selection) {
		this(-1, plugin, name, owner, coowners, selection, null);
	}
	
	public Bank(int id, BankingPlugin plugin, String name, OfflinePlayer owner, Set<OfflinePlayer> coowners,
			Selection selection, AccountConfig accountConfig) {
		this.id = id;
		this.plugin = plugin;
		this.owner = owner;
		this.coowners = coowners != null ? coowners : new HashSet<>();
		this.name = name;
		this.world = selection.getWorld();
		this.selection = selection;
		this.accounts = new HashSet<>();
		this.accountConfig = accountConfig != null ? accountConfig : new AccountConfig();
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

	public String getCoordinates() {
		if (getSelectionType().equals("CUBOID")) {
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

	public List<Location> getVertices() {
		return plugin.getBankUtils().getVertices(selection);
	}

	public String getSelectionType() {
		if (selection instanceof CuboidSelection)
			return "CUBOID";
		if (selection instanceof Polygonal2DSelection)
			return "POLYGONAL";
		return "";
	}

	public Collection<Account> getAccounts() {
		return accounts;
	}
	
	public void addAccount(Account account) {
		accounts.add(account);
	}
	
	public void addAccount(Collection<Account> newAccounts) {
		if (created)
			newAccounts.forEach(account -> accounts.add(account));
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
				// + ChatColor.GRAY + "Owner: " + getOwner().getName() + "\n"
				+ ChatColor.GRAY + "Interest rate: " + ChatColor.GREEN + Utils.formatNumber(accountConfig.getInterestRateOrDefault()) + "\n"
				+ ChatColor.GRAY + "Multipliers: " + ChatColor.AQUA + accountConfig.getMultipliersOrDefault() + "\n"
				+ ChatColor.GRAY + "Account creation price: " + ChatColor.GREEN + "$" + Utils.formatNumber(accountConfig.getAccountCreationPriceOrDefault());
	}
	
	public String toStringVerbose() {
		return toString() + "\n"
				+ ChatColor.GRAY + "Offline payouts: " + ChatColor.AQUA + accountConfig.getAllowedOfflinePayoutsOrDefault() 
						+ ChatColor.GRAY + " (" + ChatColor.AQUA + accountConfig.getAllowedOfflineBeforeResetOrDefault() + ChatColor.GRAY + " before reset)\n"
				+ ChatColor.GRAY + "Initial payout delay: " + ChatColor.AQUA + accountConfig.getInitialInterestDelayOrDefault() + "\n"
				+ ChatColor.GRAY + "Minimum balance: " + ChatColor.GREEN + "$" + Utils.formatNumber(accountConfig.getMinBalanceOrDefault()) 
						+ ChatColor.GRAY + " (" + ChatColor.RED + "$" + Utils.formatNumber(accountConfig.getLowBalanceFeeOrDefault()) + ChatColor.GRAY + " fee)\n"
				+ ChatColor.GRAY + "Current accounts: " + ChatColor.AQUA + accounts.size() + "\n"
				+ ChatColor.GRAY + "Total value: " + ChatColor.GREEN + "$" + Utils.formatNumber(getTotalValue()) + "\n"
				+ ChatColor.GRAY + "Selection type: " + getSelectionType() + "\n"
				+ ChatColor.GRAY + "Location: " + ChatColor.AQUA + getCoordinates();
	}

}
