package com.monst.bankingplugin;

import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.exceptions.ChestNotFoundException;
import com.monst.bankingplugin.exceptions.NotEnoughSpaceException;
import com.monst.bankingplugin.utils.*;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Account extends Ownable implements Nameable {

	public enum AccountSize {
		SINGLE, DOUBLE
	}

	private static final BankingPlugin plugin = BankingPlugin.getInstance();
	private boolean created;

	private final Bank bank;
	private final Location location;
	private Inventory inventory;
	private final AccountStatus status;
	private String nickname;
	private BigDecimal balance;
	private BigDecimal prevBalance;
	private AccountSize size;

	/**
	 * Creates a new account.
	 * @return the new account
	 */
	public static Account mint(OfflinePlayer owner, Location loc) {
		Bank bank = plugin.getBankUtils().getBank(loc);
		return new Account(
				-1,
				owner,
				new HashSet<>(),
				bank,
				loc,
				AccountStatus.mint(bank.getAccountConfig()),
				null,
				BigDecimal.ZERO,
				BigDecimal.ZERO
		);
	}

	/**
	 * Creates a clone of an already-existing account.
	 * @param account the account to clone
	 * @return an identical account object
	 */
	public static Account clone(Account account) {
		return new Account(
				account.getID(),
				account.getOwner(),
				account.getCoowners(),
				account.getBank(),
				account.getLocation(),
				account.getStatus(),
				account.getRawName(),
				account.getBalance(),
				account.getPrevBalance()
		);
	}

	public static Account migrate(Account account, Location newLocation) {
		return new Account(
				account.getID(),
				account.getOwner(),
				account.getCoowners(),
				plugin.getBankUtils().getBank(newLocation),
				newLocation,
				account.getStatus(),
				account.getRawName(),
				account.getBalance(),
				account.getPrevBalance()
		);
	}

	/**
	 * Re-creates an account that was stored in the {@link com.monst.bankingplugin.sql.Database}.
	 *
	 * @param id the account ID {@link Ownable}
	 * @param owner the owner of the account {@link Ownable}
	 * @param coowners the co-owners of the account {@link Ownable}
	 * @param bank the {@link Bank} the account is registered at
	 * @param loc the {@link Location} of the account chest
	 * @param status the {@link AccountStatus} of the account
	 * @param nickname the account nickname {@link Nameable}
	 * @param balance the current account balance {@link #getBalance()}
	 * @param prevBalance the previous account balance {@link #getPrevBalance()}
	 */
	public static Account reopen(int id, OfflinePlayer owner, Set<OfflinePlayer> coowners, Bank bank, Location loc,
								 AccountStatus status, String nickname, BigDecimal balance, BigDecimal prevBalance) {
		return new Account(
				id,
				owner,
				coowners,
				bank,
				loc,
				status,
				nickname,
				balance.setScale(2, RoundingMode.HALF_EVEN),
				prevBalance.setScale(2, RoundingMode.HALF_EVEN)
		);
	}

	private Account(int id, OfflinePlayer owner, Set<OfflinePlayer> coowners, Bank bank, Location loc,
					AccountStatus status, String nickname, BigDecimal balance, BigDecimal prevBalance) {

		this.id = id;
		this.owner = owner;
		this.coowners = coowners;
		this.bank = bank;
		this.location = loc;
		this.status = status;
		this.nickname = nickname;
		this.balance = balance;
		this.prevBalance = prevBalance;

	}

	/**
	 * Attempts to create this account. This method will ensure that the chest exists at
	 * the specified {@link Location} and is able to be opened.
	 * @param showConsoleMessages whether any error messages should be sent to the console
	 * @return whether this account was successfully created
	 * @see Utils#isTransparent(Block)
	 */
	public boolean create(boolean showConsoleMessages) {

		if (created) {
			plugin.debug("Account was already created! (#" + id + ")");
			return false;
		}
		plugin.debug("Creating account (#" + id + ")");

		try {
			updateInventory();
			checkSpaceAbove();
		} catch (ChestNotFoundException | NotEnoughSpaceException e) {

			plugin.getAccountUtils().removeAccount(this, Config.removeAccountOnError);

			if (showConsoleMessages)
				plugin.getLogger().severe(e.getMessage());
			plugin.debug("Failed to create account (#" + getID() + ")");
			plugin.debug(e);
			return false;
		}

		final BigDecimal checkedBalance = plugin.getAccountUtils().appraiseAccountContents(this);
		final int diff = checkedBalance.compareTo(getBalance());
		if (diff > 0)
			plugin.debug(
				getBalance().signum() == 0
					? "Cool! Account #" + getID() + " was created with a balance of "
						+ Utils.format(checkedBalance) + " already inside."
					: "Value of account #" + getID() + " was found higher than expected. Expected: $"
						+ Utils.format(getBalance()) + " but was: $" + Utils.format(checkedBalance)
			);
		else if (diff < 0)
			plugin.debug(
					"Value of account #" + id + " was found lower than expected. Expected: $"
					+ Utils.format(getBalance()) + " but was: $" + Utils.format(checkedBalance)
			);
		setBalance(checkedBalance);

		created = true;
		return true;
	}

	@Override
	public String getRawName() {
		return nickname;
	}

	/**
	 * Sets the name of this account and updates the chest inventory screen to reflect the new name.
	 * @param nickname the new name of this account.
	 */
	@Override
	public void setName(String nickname) {
		if (nickname == null)
			nickname = getDefaultName();
		this.nickname = nickname;
		if (isDoubleChest()) {
			DoubleChest dc = (DoubleChest) inventory.getHolder();
			if (dc == null)
				return;
			Chest left = (Chest) dc.getLeftSide();
			Chest right = (Chest) dc.getRightSide();
			if (left != null) {
				left.setCustomName(getColorizedName());
				left.update();
			}
			if (right != null) {
				right.setCustomName(getColorizedName());
				right.update();
			}
		} else {
			Chest chest = (Chest) inventory.getHolder();
			if (chest != null) {
				chest.setCustomName(getColorizedName());
				chest.update();
			}
		}
	}

	/**
	 * Gets the default name of this account.
	 * @return the default name of this account.
	 */
	@Override
	public String getDefaultName() {
		return ChatColor.DARK_GREEN + getOwner().getName() + "'s Account " + ChatColor.GRAY + "(#" + getID() + ")";
	}

	@Override
	public void setToDefaultName() {
		if (!hasID())
			return;
		setName(getDefaultName());
	}

	/**
	 * Resets the name in the account chest to the default, e.g. "Chest" or "Large Chest"
	 */
	public void clearChestName() {
		setName("");
	}

	/**
	 * Gets the status of this account.
	 * This includes information about the current multiplier and interest delay, among other things.
	 * @return the {@link AccountStatus} object associated with this account
	 */
	public AccountStatus getStatus() {
		return status;
	}

	/**
	 * Gets the bank this account is registered at.
	 * @return the {@link Bank} of this account.
	 */
	public Bank getBank() {
		return bank;
	}

	/**
	 * Gets this account's current balance in {@link BigDecimal} format.
	 * The balance will always be positive.
	 * @return the current account balance
	 * @see AccountUtils#appraiseAccountContents(Account)
	 */
	public BigDecimal getBalance() {
		return balance;
	}

	/**
	 * Updates the current balance of this account.
	 * Called every time the account chest is <b>closed</b> and the contents have changed.
	 * @param newBalance the new (positive) balance of the account.
	 */
	public void setBalance(BigDecimal newBalance) {
		if (newBalance != null && newBalance.signum() >= 0)
			balance = newBalance.setScale(2, RoundingMode.HALF_EVEN);
	}

	/**
	 * Gets the balance of this account as it was at the previous interest payout.
	 * @return the previous account balance.
	 */
	public BigDecimal getPrevBalance() {
		return prevBalance;
	}
	
	/**
	 * Saves the current balance of this account into the previous balance. Used
	 * only at interest payout events. Should only be used AFTER refreshing the
	 * account balance to ensure it is fully up-to-date.
	 * @see AccountUtils#appraiseAccountContents(Account)
	 */
	public void updatePrevBalance() {
		prevBalance = balance;
	}

	/**
	 * Gets the location of this account.
	 * @return the {@link Location} of the account chest.
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * Gets a nicer-looking description of the account's location.
	 * @return a {@link String} describing the location of the account chest.
	 */
	public String getCoordinates() {
		return location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
	}

	/**
	 * Ensures that the account chest is able to be opened.
	 * @throws NotEnoughSpaceException if the chest cannot be opened.
	 * @see Utils#isTransparent(Block)
	 */
	private void checkSpaceAbove() throws NotEnoughSpaceException {
		Block b = getLocation().getBlock();
		if (!Utils.isTransparent(b.getRelative(BlockFace.UP)))
			throw new NotEnoughSpaceException(
					String.format("No space above chest in world '%s' at location: %d; %d; %d", b.getWorld().getName(),
					b.getX(), b.getY(), b.getZ()));
	}

	/**
	 * Ensures that the account chest is able to be located and the inventory saved.
	 * @throws ChestNotFoundException If the chest cannot be located.
	 */
	public void updateInventory() throws ChestNotFoundException {
		Block b = getLocation().getBlock();
		if (!(b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST))
			throw new ChestNotFoundException(String.format("No chest found in world '%s' at location: %d; %d; %d",
					b.getWorld().getName(), b.getX(), b.getY(), b.getZ()));
		Chest chest = (Chest) b.getState();
		inventory = chest.getInventory();
		size = inventory.getHolder() instanceof DoubleChest ? AccountSize.DOUBLE : AccountSize.SINGLE;
	}

	/**
	 * Gets the {@link Inventory} of this account chest.
	 * @param update Whether to track down and save the inventory in the world again.
	 * @return the account inventory.
	 * @see #updateInventory()
	 */
	public Inventory getInventory(boolean update) {
		if (update) try {
			updateInventory();
		} catch (ChestNotFoundException e) {
			plugin.debug(e);
			return null;
		}
		return inventory;
	}

	/**
	 * @return Whether this account is a double chest or a single chest.
	 * @see AccountSize
	 */
	public boolean isDoubleChest() {
		return size == AccountSize.DOUBLE;
	}

	/**
	 * @return 1 if single chest, 2 if double.
	 * @see #isDoubleChest()
	 */
	public short getSize() {
		return (short) (isDoubleChest() ? 2 : 1);
	}

	@Override
	public void transferOwnership(OfflinePlayer newOwner) {
		if (newOwner == null)
			return;
		OfflinePlayer previousOwner = getOwner();
		owner = newOwner;
		if (Config.trustOnTransfer)
			coowners.add(previousOwner);
	}

	@Override
	@SuppressWarnings("deprecation")
	public TextComponent getInfoButton(CommandSender sender) {
		TextComponent button = new TextComponent("[Info]");
		button.setColor(net.md_5.bungee.api.ChatColor.GRAY);
		ComponentBuilder cb = new ComponentBuilder()
				.append(ChatColor.GRAY + "Bank: " + getBank().getColorizedName())
				.append(ChatColor.GRAY + "Owner: " + getOwnerDisplayName())
				.append(ChatColor.GRAY + "Click for more info.");
		button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, cb.create()));
		button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/account info " + getID()));
		return button;
	}

	@Override
	public TextComponent getInformation(CommandSender sender) {
		boolean isOwner = sender instanceof Player && isOwner((Player) sender);
		boolean verbose = (sender instanceof Player && (isTrusted((Player) sender) || getBank().isTrusted((Player) sender)))
				|| sender.hasPermission(Permissions.ACCOUNT_INFO_OTHER);

		TextComponent info = new TextComponent();
		info.setColor(net.md_5.bungee.api.ChatColor.GRAY);

		info.addExtra("\"" + Utils.colorize(getRawName()) + ChatColor.GRAY + "\"");
		info.addExtra("\n    Bank: " + ChatColor.RED + getBank().getColorizedName());
		if (!isOwner)
			info.addExtra("\n    Owner: " + ChatColor.GOLD + getOwnerDisplayName());
		if (!getCoowners().isEmpty())
			info.addExtra("\n    Co-owners: " + getCoowners().stream().map(OfflinePlayer::getName).collect(Collectors.joining(", ", "[", "]")));
		if (verbose) {
			info.addExtra("\n    Balance: " + ChatColor.GREEN + "$" + Utils.format(getBalance()));
			info.addExtra("\n    Multiplier: " + ChatColor.AQUA + getStatus().getRealMultiplier() + ChatColor.GRAY + " (Stage " + getStatus().getMultiplierStage() + ")");
			TextComponent interestRate = new TextComponent("\n    Interest rate: ");
			double interestR = getBank().getAccountConfig().get(AccountConfig.Field.INTEREST_RATE);
			interestRate.addExtra(ChatColor.GREEN + "" + BigDecimal.valueOf(interestR * getStatus().getRealMultiplier() * 100)
					.setScale(1, BigDecimal.ROUND_HALF_EVEN)
					+ "% " + ChatColor.GRAY + "(" + interestR + " x " + getStatus().getRealMultiplier() + ")");
			if (getStatus().getDelayUntilNextPayout() != 0)
				interestRate.addExtra(ChatColor.RED + " (" + getStatus().getDelayUntilNextPayout() + " payouts to go)");
			info.addExtra(interestRate);
		}
		info.addExtra("\n    Location: " + ChatColor.AQUA + "(" + getCoordinates() + ")");

		return info;
	}

	@Override
	public String toString() {
		   return "ID: " + getID() + ", "
				+ "Owner: " + getOwner().getName() + ", "
				+ "Bank: " + getBank().getName() + ", "
				+ "Balance: $" + Utils.format(getBalance()) + ", "
				+ "Previous balance: $" + Utils.format(getPrevBalance()) + ", "
				+ "Multiplier: " + getStatus().getRealMultiplier() + ", "
					+ " (stage " + getStatus().getMultiplierStage() + "), "
				+ "Delay until next payout: " + getStatus().getDelayUntilNextPayout() + ", "
				+ "Next payout amount: " + Utils.format(getBalance().doubleValue()
						* (double) getBank().getAccountConfig().get(AccountConfig.Field.INTEREST_RATE)
						* getStatus().getRealMultiplier()) + ", "
				+ "Location: " + getCoordinates();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Account otherAccount = (Account) o;
		return getID() != -1 && getID() == otherAccount.getID();
	}

	@Override
	public int hashCode() {
		return getID() != -1 ? getID() : super.hashCode();
	}
}
