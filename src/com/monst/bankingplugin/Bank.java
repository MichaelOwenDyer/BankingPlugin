package com.monst.bankingplugin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import net.md_5.bungee.api.chat.TextComponent;

public class Bank extends Ownable {
	
	public enum BankType {
		PLAYER, ADMIN
	}

	private final BankingPlugin plugin;
	private boolean created;
	
	private String name;
	private final World world;
	private Selection selection;
	private final AccountConfig accountConfig;
	private final Set<Account> accounts;
	private BankType type;

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
	
	public void addAccount(Account account) {
		if (account != null)
			accounts.add(account);
	}

	public void removeAccount(Account account) {
		if (account != null)
			accounts.remove(account);
	}

	public void removeAccount(Collection<Account> accountsToRemove) {
		if (accountsToRemove != null)
			accountsToRemove.forEach(account -> removeAccount(account));
	}

	public BigDecimal getTotalValue() {
		if (created)
			return accounts.stream().map(account -> account.getBalance()).reduce(BigDecimal.ZERO,
					(value, sum) -> sum.add(value)).setScale(2, RoundingMode.HALF_EVEN);
		else
			return BigDecimal.ZERO;
	}

	/**
	 * Calculates Gini coefficient of this bank. This is a measurement of wealth
	 * inequality among all n accounts at the bank.
	 * 
	 * @return G = ( 2 * sum(i,n)(i * value of ith account) / n * sum(i,n)(value of
	 *         ith account) ) - ( n + 1 / n )
	 */
	public double getGiniCoefficient() {
		if (getAccounts().isEmpty())
			return 0;
		List<BigDecimal> orderedValues = getCustomerBalances().values().stream().sorted(BigDecimal::compareTo)
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
		BigDecimal leftEq = weightedValueSum.divide(valueSum);
		BigDecimal rightEq = BigDecimal.valueOf((orderedValues.size() + 1) / orderedValues.size());
		return leftEq.subtract(rightEq).doubleValue();
	}
	
	@SuppressWarnings("unchecked")
	public TextComponent getInfo() {
		
		TextComponent info = new TextComponent("\"" + ChatColor.GOLD + Utils.colorize(name) + ChatColor.GRAY + "\" (#" + id + ")\n");
		info.setColor(net.md_5.bungee.api.ChatColor.GRAY);
		TextComponent owner = new TextComponent("Owner: " + (isAdminBank() ? ChatColor.RED + "ADMIN" : getOwnerDisplayName()) + "\n");
		TextComponent interestRate = new TextComponent("Interest rate: " + ChatColor.GREEN + Utils.formatNumber((double) accountConfig.getOrDefault(Field.INTEREST_RATE)) + "\n");
		TextComponent multipliers = new TextComponent("Multipliers: ");
		multipliers.addExtra(Utils.getMultiplierView((List<Integer>) accountConfig.getOrDefault(Field.MULTIPLIERS)));
		TextComponent creationPrice = new TextComponent("\nAccount creation price: " + ChatColor.GREEN + "$"
				+ Utils.formatNumber((double) accountConfig.getOrDefault(Field.ACCOUNT_CREATION_PRICE)));
		
		info.addExtra(owner);
		info.addExtra(interestRate);
		info.addExtra(multipliers);
		info.addExtra(creationPrice);
		
		return info;
		
	}
	
	public TextComponent getInfoVerbose() {
		double minBal = (double) accountConfig.getOrDefault(Field.MINIMUM_BALANCE);
		
		TextComponent info = getInfo();
		TextComponent offlinePayouts = new TextComponent(
				"\nOffline payouts: " + ChatColor.AQUA + accountConfig.getOrDefault(Field.ALLOWED_OFFLINE_PAYOUTS));
		offlinePayouts.addExtra(ChatColor.GRAY + " (" + ChatColor.AQUA + accountConfig.getOrDefault(Field.ALLOWED_OFFLINE_PAYOUTS_BEFORE_MULTIPLIER_RESET) + ChatColor.GRAY + " before reset)\n");
		TextComponent initialDelay = new TextComponent("Initial payout delay: " + ChatColor.AQUA + accountConfig.getOrDefault(Field.INITIAL_INTEREST_DELAY) + "\n");
		TextComponent minBalance = new TextComponent("Minimum balance: " + ChatColor.GREEN + "$" + Utils.formatNumber(minBal));
		if (minBal != 0)
			minBalance.addExtra(" (" + ChatColor.RED + "$" + Utils.formatNumber((double) accountConfig.getOrDefault(Field.LOW_BALANCE_FEE)) + ChatColor.GRAY + " fee)" + "\n");
		TextComponent numberOfAccounts = new TextComponent("Current accounts: " + ChatColor.AQUA + accounts.size() + "\n");
		TextComponent totalValue = new TextComponent("Total value: " + ChatColor.GREEN + "$" + Utils.formatNumber(getTotalValue()) + "\n");
		TextComponent equality = new TextComponent("Inequality score: " + String.format("%.2f", getGiniCoefficient()) + "\n"); // TODO: Dynamic color
		TextComponent selectionType = new TextComponent("Selection type: " + selection.getType() + "\n");
		TextComponent loc = new TextComponent("Location: " + ChatColor.AQUA + getCoordinates());
		
		info.addExtra(offlinePayouts);
		info.addExtra(initialDelay);
		info.addExtra(minBalance);
		info.addExtra(numberOfAccounts);
		info.addExtra(totalValue);
		info.addExtra(equality);
		info.addExtra(selectionType);
		info.addExtra(loc);
		
		return info;

	}

	@SuppressWarnings("unchecked")
	@Override
	public String toString() {
		return ChatColor.GRAY + "\"" + ChatColor.GOLD + Utils.colorize(name) + ChatColor.GRAY + "\" (#" + id + ")\n"
				+ ChatColor.GRAY + "Owner: " + (isAdminBank() ? ChatColor.RED + "ADMIN" : getOwnerDisplayName()) + "\n"
				+ ChatColor.GRAY + "Interest rate: " + ChatColor.GREEN + Utils.formatNumber((double) accountConfig.getOrDefault(Field.INTEREST_RATE)) + "\n"
				+ ChatColor.GRAY + "Multipliers:" + Utils.getMultiplierView((List<Integer>) accountConfig.getOrDefault(Field.MULTIPLIERS)) + "\n"
				+ ChatColor.GRAY + "Account creation price: " + ChatColor.GREEN + "$" + Utils.formatNumber((double) accountConfig.getOrDefault(Field.ACCOUNT_CREATION_PRICE));
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public Collection<Account> getAccounts() {
		return accounts;
	}

	public Map<OfflinePlayer, List<Account>> getCustomerAccounts() {
		return getAccounts().stream().collect(Collectors.groupingBy(Account::getOwner));
	}

	public Map<OfflinePlayer, BigDecimal> getCustomerBalances() {
		Map<OfflinePlayer, BigDecimal> customerBalances = new HashMap<>();
		getCustomerAccounts().entrySet().forEach(entry -> {
			customerBalances.put(entry.getKey(),
					entry.getValue().stream().map(Account::getBalance).reduce(BigDecimal.ZERO, (a, bd) -> a.add(bd)));
		});
		return customerBalances;
	}

	public void setBankType(BankType type) {
		this.type = type;
	}
}
