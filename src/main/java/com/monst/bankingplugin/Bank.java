package com.monst.bankingplugin;

import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.selections.Selection;
import com.monst.bankingplugin.utils.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class Bank extends Ownable implements Nameable {

	/**
	 * Banks are either owned and operated by players or by the admins.
	 * Admin banks cannot run out of money, whereas a player bank relies on its owner to pay interest to the customers.
	 */
	public enum BankType {
		PLAYER, ADMIN
	}

	private static final BankingPlugin plugin = BankingPlugin.getInstance();

	private String name;
	private Selection selection;
	private final AccountConfig accountConfig;
	private final Set<Account> accounts;
	private BankType type;

	/**
	 * Create a new admin bank.
	 */
	public static Bank mint(String name, Selection selection) {
		return new Bank(
				-1,
				name,
				null,
				new HashSet<>(),
				selection,
				new AccountConfig(),
				BankType.ADMIN
		);
	}

	/**
	 * Create a new player bank.
	 */
	public static Bank mint(String name, OfflinePlayer owner, Selection selection) {
		return new Bank(
				-1,
				name,
				owner,
				new HashSet<>(),
				selection,
				new AccountConfig(),
				BankType.PLAYER
		);
	}

	/**
	 * Re-create an admin bank that was stored in the {@link com.monst.bankingplugin.sql.Database}.
	 */
	public static Bank recreate(int id, String name, Set<OfflinePlayer> coowners,
								Selection selection, AccountConfig accountConfig) {
		return new Bank(
				id,
				name,
				null,
				coowners,
				selection,
				accountConfig,
				BankType.ADMIN
		);
	}

	/**
	 * Re-create a player bank that was stored in the {@link com.monst.bankingplugin.sql.Database}.
	 */
	public static Bank recreate(int id, String name, OfflinePlayer owner, Set<OfflinePlayer> coowners,
								Selection selection, AccountConfig accountConfig) {
		return new Bank(
				id,
				name,
				owner,
				coowners,
				selection,
				accountConfig,
				BankType.PLAYER
		);
	}

	/**
	 * @param id the bank ID {@link Ownable}
	 * @param name the name of the bank {@link Nameable}
	 * @param owner the owner of the bank {@link Ownable}
	 * @param coowners the co-owners of the bank {@link Ownable}
	 * @param selection the {@link Selection} representing the bounds of the bank
	 * @param accountConfig the {@link AccountConfig} of the bank
	 * @param type the {@link BankType} of the bank
	 */
	private Bank(int id, String name, OfflinePlayer owner, Set<OfflinePlayer> coowners,
			Selection selection, AccountConfig accountConfig, BankType type) {

		this.id = id;
		this.owner = owner;
		this.coowners = coowners;
		this.name = name;
		this.selection = selection;
		this.accounts = new HashSet<>();
		this.accountConfig = accountConfig;
		this.type = type;

	}

	/**
	 * Add an account to this bank.
	 * @param account the account to be added
	 */
	public void addAccount(Account account) {
		removeAccount(account);
		if (account != null)
			accounts.add(account);
	}

	/**
	 * Remove an account from this bank.
	 * @param account the account to be removed
	 */
	public void removeAccount(Account account) {
		if (account != null)
			accounts.remove(account);
	}

	/**
	 * Calculate the sum of all {@link Account} balances at this bank.
	 * @return the total value of the accounts at this bank
	 * @see Account#getBalance()
	 */
	public BigDecimal getTotalValue() {
		return accounts.stream().map(Account::getBalance).reduce(BigDecimal.ZERO,
					(value, sum) -> sum.add(value));
	}

	/**
	 * @return a {@link Collection<Account>} containing all accounts at this bank
	 */
	public Collection<Account> getAccounts() {
		return accounts;
	}

	/**
	 * This is the same as {@link #getAccounts()} but is safe
	 * to use for removing all accounts from the bank.
	 * @return a {@link Collection<Account>} containing a copy of all accounts at this bank
	 */
	public Collection<Account> getAccountsCopy() {
		return Collections.unmodifiableCollection(getAccounts());
	}

	/**
	 * @return a {@link Map<OfflinePlayer>} containing
	 * all accounts at this bank grouped by owner
	 */
	public Map<OfflinePlayer, List<Account>> getCustomerAccounts() {
		return getAccounts().stream().collect(Collectors.groupingBy(Account::getOwner));
	}

	/**
	 * @return a {@link Set<OfflinePlayer>} containing all account owners
	 * and account co-owners at this bank.
	 */
	public Set<OfflinePlayer> getCustomers() {
		return getAccounts().stream().map(Account::getTrustedPlayers)
				.flatMap(Collection::stream).collect(Collectors.toSet());
	}

	/**
	 * @return a {@link Map<OfflinePlayer>} containing
	 * all account owners at this bank and their total account balances
	 */
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
	 * @param name the new name of this bank
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
	public void setToDefaultName() {
		if (!hasID())
			return;
		setName(getDefaultName());
	}

	/**
	 * @return the {@link Selection} representing the bounds of this bank
	 */
	public Selection getSelection() {
		return selection;
	}

	/**
	 * @param sel the new {@link Selection} to represent the bounds of this bank
	 */
	public void setSelection(Selection sel) {
		this.selection = sel;
	}

	/**
	 * @return the {@link AccountConfig} of this bank
	 */
	public AccountConfig getAccountConfig() {
		return accountConfig;
	}

	/**
	 * @return the {@link BankType} of this bank
	 */
	public BankType getType() {
		return type;
	}

	/**
	 * @param type the new {@link BankType} of this bank
	 */
	public void setType(BankType type) {
		this.type = type;
	}

	/**
	 * @return whether the bank is an admin bank
	 * @see BankType
	 */
	public boolean isAdminBank() {
		return getType() == BankType.ADMIN;
	}

	/**
	 * @return whether the bank is a player bank
	 * @see BankType
	 */
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
	@SuppressWarnings("deprecation")
	public TextComponent getInfoButton(CommandSender sender) {
		TextComponent button = new TextComponent("[Info]");
		button.setColor(net.md_5.bungee.api.ChatColor.GRAY);
		button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
				new ComponentBuilder().append(org.bukkit.ChatColor.GRAY + "Click for bank info.").create()));
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
			info.addExtra("\n    Owner: " + getOwnerDisplayName());
		if (!getCoowners().isEmpty())
			info.addExtra("\n    Co-owners: " + getCoowners().stream().map(OfflinePlayer::getName).collect(Collectors.joining(", ", "[", "]")));
		info.addExtra("\n    Interest rate: " + ChatColor.GREEN + accountConfig.getFormatted(AccountConfig.Field.INTEREST_RATE));
		info.addExtra("\n    Multipliers: ");
		info.addExtra(Utils.getMultiplierView(this));
		info.addExtra("\n    Account creation price: " + ChatColor.GREEN + "$" + accountConfig.getFormatted(AccountConfig.Field.ACCOUNT_CREATION_PRICE));
		info.addExtra("\n    Offline payouts: " + ChatColor.AQUA + accountConfig.get(AccountConfig.Field.ALLOWED_OFFLINE_PAYOUTS));
		info.addExtra(" (" + ChatColor.AQUA + accountConfig.get(AccountConfig.Field.ALLOWED_OFFLINE_PAYOUTS_BEFORE_RESET) + ChatColor.GRAY + " before multiplier reset)");
		info.addExtra("\n    Initial payout delay: " + ChatColor.AQUA + accountConfig.get(AccountConfig.Field.INITIAL_INTEREST_DELAY));
		double minBal = accountConfig.get(AccountConfig.Field.MINIMUM_BALANCE);
		info.addExtra("\n    Minimum balance: " + ChatColor.GREEN + "$" + Utils.format(minBal));
		if (minBal != 0)
			info.addExtra(" (" + ChatColor.RED + "$" + accountConfig.getFormatted(AccountConfig.Field.LOW_BALANCE_FEE) + ChatColor.GRAY + " fee)");
		if (verbose) {
			info.addExtra("\n    Accounts: " + ChatColor.AQUA + accounts.size());
			info.addExtra("\n    Total value: " + ChatColor.GREEN + "$" + Utils.format(getTotalValue()));
			info.addExtra("\n    Average account value: " + ChatColor.GREEN + "$" + Utils.format(getTotalValue().doubleValue() / accounts.size()));
			info.addExtra("\n    Equality score: ");
			info.addExtra(BankUtils.getEqualityLore(this));
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
				+ "\nTotal value: " + Utils.format(getTotalValue())
				+ "\nEquality score: " + BankUtils.getEqualityLore(this)
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
