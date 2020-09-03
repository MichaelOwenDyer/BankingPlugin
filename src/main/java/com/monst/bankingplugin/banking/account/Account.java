package com.monst.bankingplugin.banking.account;

import com.monst.bankingplugin.banking.Ownable;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.banking.bank.BankField;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.exceptions.ChestNotFoundException;
import com.monst.bankingplugin.exceptions.NotEnoughSpaceException;
import com.monst.bankingplugin.utils.AccountUtils;
import com.monst.bankingplugin.utils.Nameable;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.Inventory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;

public class Account extends Ownable {

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
				AccountStatus.mint(bank),
				ChatColor.DARK_GREEN + owner.getName() + "'s Account",
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

	/**
	 * Re-creates an account that was stored in the {@link com.monst.bankingplugin.sql.Database}.
	 *
	 * @param id the account ID {@link Ownable}
	 * @param owner the owner of the account {@link Ownable}
	 * @param coowners the co-owners of the account {@link Ownable}
	 * @param bank the {@link Bank} the account is registered at
	 * @param loc the {@link Location} of the account chest
	 * @param status the {@link AccountStatus} of the account
	 * @param name the account name {@link Nameable}
	 * @param balance the current account balance {@link #getBalance()}
	 * @param prevBalance the previous account balance {@link #getPrevBalance()}
	 */
	public static Account reopen(int id, OfflinePlayer owner, Set<OfflinePlayer> coowners, Bank bank, Location loc,
								 AccountStatus status, String name, BigDecimal balance, BigDecimal prevBalance) {
		return new Account(
				id,
				owner,
				coowners,
				bank,
				loc,
				status,
				name,
				balance.setScale(2, RoundingMode.HALF_EVEN),
				prevBalance.setScale(2, RoundingMode.HALF_EVEN)
		);
	}

	public enum AccountSize {
		SINGLE, DOUBLE
	}

	private boolean created;

	private Bank bank;
	private Location location;
	private final AccountStatus status;
	private Inventory inventory;
	private BigDecimal balance;
	private BigDecimal prevBalance;
	private AccountSize size;

	private Account(int id, OfflinePlayer owner, Set<OfflinePlayer> coowners, Bank bank, Location loc,
					AccountStatus status, String name, BigDecimal balance, BigDecimal prevBalance) {

		this.id = id;
		this.owner = owner;
		this.coowners = coowners;
		this.bank = bank;
		this.location = loc;
		this.status = status;
		this.name = name;
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
			plugin.debug("Account was already created! (#" + getID() + ")");
			return false;
		}
		plugin.debug("Creating account (#" + getID() + ")");

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
					"Value of account #" + getID() + " was found lower than expected. Expected: $"
					+ Utils.format(getBalance()) + " but was: $" + Utils.format(checkedBalance)
			);
		setBalance(checkedBalance);

		updateName();

		created = true;
		return true;
	}

	/**
	 * Gets the bank this account is registered at.
	 * @return the {@link Bank} of this account.
	 */
	public Bank getBank() {
		return bank;
	}

	/**
	 * Sets the bank of this account.
	 * @param bank the new {@link Bank} of this account.
	 */
	public void setBank(Bank bank) {
		getBank().removeAccount(this);
		this.bank = bank;
		getBank().addAccount(this);
		this.getStatus().setBank(bank);
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
	 * Saves the current balance of this account into the previous balance.
	 * Used only at interest payout events.
	 * @see AccountUtils#appraiseAccountContents(Account)
	 * @see com.monst.bankingplugin.listeners.InterestEventListener
	 */
	public void updatePrevBalance() {
		prevBalance = balance;
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
	 * Gets the location of this account.
	 * @return the {@link Location} of the account chest.
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * Sets the location of this account.
	 * @param location the new location
	 */
	public void setLocation(Location location) {
		this.location = location;
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
		if (getInventory(true) == null)
			throw new ChestNotFoundException(String.format("No chest found in world '%s' at location: %d; %d; %d",
					b.getWorld().getName(), b.getX(), b.getY(), b.getZ()));
	}

	/**
	 * Gets the {@link Inventory} of this account chest.
	 * @return the account inventory.
	 * @see #updateInventory()
	 */
	public Inventory getInventory(boolean update) {
		if (!update)
			return inventory;
		Block b = getLocation().getBlock();
		if (b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST) {
			Chest chest = (Chest) b.getState();
			inventory = chest.getInventory();
			setSize(inventory.getHolder() instanceof DoubleChest ? AccountSize.DOUBLE : AccountSize.SINGLE);
			return inventory;
		}
		return null;
	}

	public void setSize(AccountSize size) {
		this.size = size;
	}

	/**
	 * @return 1 if single chest, 2 if double.
	 */
	public short getSize() {
		return (short) (size == AccountSize.DOUBLE ? 2 : 1);
	}

	/**
	 * Sets the name of this account and updates the chest inventory screen to reflect the new name.
	 * @param name the new name of this account.
	 */
	@Override
	public void setName(String name) {
		if (name == null)
			name = getDefaultName();
		this.name = name;
		Inventory inv = getInventory(true);
		if (inv == null)
			return;
		if (size == AccountSize.DOUBLE) {
			DoubleChest dc = (DoubleChest) inv.getHolder();
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
			Chest chest = (Chest) inv.getHolder();
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
	public String getDefaultName() {
		return ChatColor.DARK_GREEN + getOwner().getName() + "'s Account";
	}

	public void setToDefaultName() {
		setName(getDefaultName());
	}

	/**
	 * @return whether this account is currently using its default name
	 */
	public boolean isDefaultName() {
		return getRawName().contentEquals(getDefaultName());
	}

	/**
	 * Ensures that the current name is valid and currently being reflected everywhere it should be.
	 * If the current name is null, set it to the default name.
	 */
	public void updateName() {
		if (getRawName() != null)
			setName(getRawName());
		else
			setToDefaultName();
	}

	/**
	 * Resets the name in the account chest to the default, e.g. "Chest" or "Large Chest"
	 */
	public void clearChestName() {
		setName("");
	}

	@Override
	public void transferOwnership(OfflinePlayer newOwner) {
		if (newOwner == null)
			return;
		OfflinePlayer previousOwner = getOwner();
		owner = newOwner;
		if (Config.trustOnTransfer)
			coowners.add(previousOwner);
		untrustPlayer(owner); // Remove from co-owners if new owner was a co-owner
	}

	@Override
	public String getInformation() {

		StringBuilder info = new StringBuilder(196);

		info.append("" + ChatColor.GRAY);
		info.append("\"" + Utils.colorize(getRawName()) + ChatColor.GRAY + "\"");
		info.append(ChatColor.GRAY + "Bank: " + ChatColor.RED + getBank().getColorizedName());
		info.append(ChatColor.GRAY + "Owner: " + ChatColor.GOLD + getOwnerDisplayName());
		if (!getCoowners().isEmpty())
			info.append(ChatColor.GRAY + "Co-owners: " + Utils.map(getCoowners(), OfflinePlayer::getName).toString());
		info.append(ChatColor.GRAY + "Balance: " + ChatColor.GREEN + "$" + Utils.format(getBalance()));
		info.append(ChatColor.GRAY + "Multiplier: " + ChatColor.AQUA + getStatus().getRealMultiplier()
				+ ChatColor.GRAY + " (Stage " + getStatus().getMultiplierStage() + ")");
		StringBuilder interestRate = new StringBuilder(ChatColor.GRAY + "Interest rate: ");
		double interestR = getBank().get(BankField.INTEREST_RATE);
		interestRate.append(ChatColor.GREEN + "" + BigDecimal.valueOf(interestR * getStatus().getRealMultiplier() * 100)
				.setScale(1, BigDecimal.ROUND_HALF_EVEN)
				+ "% " + ChatColor.GRAY + "(" + interestR + " x " + getStatus().getRealMultiplier() + ")");
		if (getStatus().getDelayUntilNextPayout() != 0)
			interestRate.append(ChatColor.RED + " (" + getStatus().getDelayUntilNextPayout() + " payouts to go)");
		info.append(interestRate.toString());
		info.append(ChatColor.GRAY + "Location: " + ChatColor.AQUA + "(" + getCoordinates() + ")");

		return info.toString();
	}

	@Override
	public String toString() {
		   return "Account ID: " + getID() + ", "
				+ "Owner: " + getOwner().getName() + ", "
				+ "Bank: " + getBank().getName() + ", "
				+ "Balance: $" + Utils.format(getBalance()) + ", "
				+ "Previous balance: $" + Utils.format(getPrevBalance()) + ", "
				+ "Multiplier: " + getStatus().getRealMultiplier() + ", "
					+ " (stage " + getStatus().getMultiplierStage() + "), "
				+ "Delay until next payout: " + getStatus().getDelayUntilNextPayout() + ", "
				+ "Next payout amount: " + Utils.format(getBalance().doubleValue()
						* (double) getBank().get(BankField.INTEREST_RATE)
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