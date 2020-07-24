package com.monst.bankingplugin;

import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.selections.Selection;
import com.monst.bankingplugin.utils.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class Bank extends Ownable implements Nameable {
	
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
	public Bank(BankingPlugin plugin, Selection selection) {
		this(-1, plugin, null, null, null, selection, new AccountConfig(), BankType.ADMIN);
	}

	// Old admin bank
	public Bank(int id, BankingPlugin plugin, String name, Selection selection, AccountConfig config) {
		this(id, plugin, name, null, null, selection, config, BankType.ADMIN);
	}

	// New player bank
	public Bank(BankingPlugin plugin, OfflinePlayer owner, Set<OfflinePlayer> coowners,
			Selection selection) {
		this(-1, plugin, null, owner, coowners, selection, new AccountConfig(), BankType.PLAYER);
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
	
	public boolean create() {
		if (created) {
			plugin.debug("Bank was already created! (#" + id + ")");
			return false;
		}
		plugin.debug("Creating bank (#" + id + ")");
		created = true;
		return true;
	}

	public void addAccount(Account account) {
		removeAccount(account);
		if (account != null)
			accounts.add(account);
	}

	public void removeAccount(Account account) {
		if (account != null)
			accounts.remove(account);
	}

	public BigDecimal getTotalValue() {
		if (created)
			return accounts.stream().map(Account::getBalance).reduce(BigDecimal.ZERO,
					(value, sum) -> sum.add(value)).setScale(2, RoundingMode.HALF_EVEN);
		else
			return BigDecimal.ZERO;
	}

	public Collection<Account> getAccounts() {
		return accounts;
	}

	public Collection<Account> getAccountsCopy() {
		return Collections.unmodifiableCollection(getAccounts());
	}

	public Map<OfflinePlayer, List<Account>> getCustomerAccounts() {
		return getAccounts().stream().collect(Collectors.groupingBy(Account::getOwner));
	}

	public Map<OfflinePlayer, BigDecimal> getCustomerBalances() {
		Map<OfflinePlayer, BigDecimal> customerBalances = new HashMap<>();
		getCustomerAccounts().forEach((key, value) -> customerBalances.put(key,
				value.stream().map(Account::getBalance).reduce(BigDecimal.ZERO, BigDecimal::add)));
		return customerBalances;
	}

	@Override
	public String getRawName() {
		return name;
	}

	/**
	 * Sets the name of this bank and updates the value in the database.
	 * @param name The new name of this bank.
	 */
	@Override
	public void setName(String name) {
		this.name = name;
		plugin.getBankUtils().addBank(this, true); // Update bank in database
	}

	@Override
	public String getDefaultName() {
		return ChatColor.RED + (isAdminBank() ? "Admin" : "") + "Bank" + ChatColor.GRAY + "(#" + getID() + ")";
	}

	@Override
	public void resetName() {
		if (!hasID())
			return;
		setName(getDefaultName());
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

	public void setType(BankType type) {
		this.type = type;
	}

	public boolean isAdminBank() {
		return getType() == BankType.ADMIN;
	}

	public boolean isPlayerBank() {
		return getType() == BankType.PLAYER;
	}

	@Override
	public void transferOwnership(OfflinePlayer newOwner) {
		OfflinePlayer prevOwner = getOwner();
		owner = newOwner;
		setType(newOwner == null ? BankType.ADMIN : BankType.PLAYER);
		if (Config.trustOnTransfer)
			coowners.add(prevOwner);
	}

	@Override
	public TextComponent getInfoButton(CommandSender sender) {
		TextComponent button = new TextComponent("[Info]");
		button.setColor(net.md_5.bungee.api.ChatColor.GRAY);
		button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(org.bukkit.ChatColor.GRAY + "Click for bank info.")));
		button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bank info " + getID()));
		return button;
	}

	@Override
	public TextComponent getInformation(CommandSender sender) {
		boolean isOwner = sender instanceof Player && isOwner((Player) sender);
		boolean verbose = (sender instanceof Player && isTrusted((Player) sender))
				|| (getType() == BankType.PLAYER && sender.hasPermission(Permissions.BANK_INFO_OTHER)
				|| (getType() == BankType.ADMIN && sender.hasPermission(Permissions.BANK_INFO_ADMIN)));
		
		TextComponent info = new TextComponent();
		info.setColor(net.md_5.bungee.api.ChatColor.GRAY);
		
		info.addExtra("\"" + ChatColor.RED + getColorizedName() + ChatColor.GRAY + "\" (#" + id + ")");
		if (!isOwner)
			info.addExtra("\n    Owner: " + (isPlayerBank() ? getOwnerDisplayName() : ChatColor.RED + "ADMIN"));
		if (!getCoowners().isEmpty())
			info.addExtra("\n    Co-owners: " + getCoowners().stream().map(OfflinePlayer::getName).collect(Collectors.joining(", ", "[", "]")));
		info.addExtra("\n    Interest rate: " + ChatColor.GREEN + Utils.formatNumber(accountConfig.getInterestRate(false)));
		info.addExtra("\n    Multipliers: ");
		info.addExtra(Utils.getMultiplierView(this));
		info.addExtra("\n    Account creation price: " + ChatColor.GREEN + "$" + Utils.formatNumber(accountConfig.getAccountCreationPrice(false)));
		info.addExtra("\n    Offline payouts: " + ChatColor.AQUA + accountConfig.getAllowedOfflinePayouts(false));
		info.addExtra(" (" + ChatColor.AQUA + accountConfig.getAllowedOfflineBeforeReset(false) + ChatColor.GRAY + " before multiplier reset)");
		info.addExtra("\n    Initial payout delay: " + ChatColor.AQUA + accountConfig.getInitialInterestDelay(false));
		double minBal = accountConfig.getMinBalance(false);
		info.addExtra("\n    Minimum balance: " + ChatColor.GREEN + "$" + Utils.formatNumber(minBal));
		if (minBal != 0)
			info.addExtra(" (" + ChatColor.RED + "$" + Utils.formatNumber(accountConfig.getLowBalanceFee(false)) + ChatColor.GRAY + " fee)");
		if (verbose) {
			info.addExtra("\n    Accounts: " + ChatColor.AQUA + accounts.size());
			info.addExtra("\n    Total value: " + ChatColor.GREEN + "$" + Utils.formatNumber(getTotalValue()));
			info.addExtra("\n    Average account value: " + ChatColor.GREEN + "$" + Utils.formatNumber(getTotalValue().doubleValue() / accounts.size()));
			info.addExtra("\n    Equality score: ");
			info.addExtra(Utils.getEqualityLore(this));
		}
		info.addExtra("\n    Location: " + ChatColor.AQUA + getSelection().getCoordinates());

		return info;
	}

	@Override
	public String toString() {
		return "ID: " + getID()
				+ "\nName: " + getName() + " (Raw: " + getRawName() + ")"
				+ "\nOwner: " + (isPlayerBank() ? getOwner().getName() : "ADMIN")
				+ "\nNumber of accounts: " + getAccounts().size()
				+ "\nTotal value: " + Utils.formatNumber(getTotalValue())
				+ "\nEquality score: " + Utils.getEqualityLore(this)
				+ "\nSelection type: " + getSelection().getType()
				+ "\nLocation: " + getSelection().getCoordinates();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Bank otherBank = (Bank) o;
		return getID() != -1 && getID() == otherBank.getID();
	}

	@Override
	public int hashCode() {
		return getID() != -1 ? getID() : super.hashCode();
	}
}
