package com.monst.bankingplugin.banking.account;

import com.monst.bankingplugin.banking.BankingEntity;
import com.monst.bankingplugin.banking.Nameable;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.exceptions.ChestBlockedException;
import com.monst.bankingplugin.exceptions.ChestNotFoundException;
import com.monst.bankingplugin.geo.locations.ChestLocation;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.QuickMath;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Account extends BankingEntity {

	private static final String DEFAULT_NAME = ChatColor.DARK_GREEN + "%s's Account";
	private static final String DEFAULT_CHEST_NAME = DEFAULT_NAME + ChatColor.GRAY + " (#%d)";

	/**
	 * Creates a new account.
	 *
	 * @return the new account
	 */
	public static Account mint(OfflinePlayer owner, ChestLocation loc) {
		Bank bank = plugin.getBankRepository().getAt(loc);
		return new Account(
				-1,
				owner,
				new HashSet<>(),
				bank,
				loc,
				String.format(DEFAULT_NAME, owner.getName()),
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				0,
				bank.getInitialInterestDelay().get(),
				bank.getAllowedOfflinePayouts().get(),
				bank.getAllowedOfflinePayoutsBeforeReset().get()
		);
	}

	/**
	 * Creates a clone of an already-existing account.
	 *
	 * @param account the account to clone
	 * @return an identical account object
	 */
	public static Account clone(Account account) {
		return new Account(
				account.getID(),
				account.getOwner(),
				account.getCoOwners(),
				account.getBank(),
				account.getChestLocation(),
				account.getRawName(),
				account.getBalance(),
				account.getPrevBalance(),
				account.getMultiplierStage(),
				account.getDelayUntilNextPayout(),
				account.getRemainingOfflinePayouts(),
				account.getRemainingOfflinePayoutsUntilReset()
		);
	}

	/**
	 * Re-creates an account that was stored in the database.
	 *
	 * @param id the account ID {@link BankingEntity}
	 * @param owner the owner of the account {@link BankingEntity}
	 * @param coowners the co-owners of the account {@link BankingEntity}
	 * @param bank the {@link Bank} the account is registered at
	 * @param loc the {@link ChestLocation} of the account chest
	 * @param name the account name {@link Nameable}
	 * @param balance the current account balance {@link #getBalance()}
	 * @param prevBalance the previous account balance {@link #getPrevBalance()}
	 * @param multiplierStage the multiplier stage of this account
	 * @param delayUntilNextPayout the number of payments this account will wait before generating interest
	 * @param remainingOfflinePayouts the number of remaining offline interest payments this account will generate
	 * @param remainingOfflineUntilReset the number of remaining offline interest payments before the multiplier stage is reset
	 */
	public static Account reopen(int id, OfflinePlayer owner, Set<OfflinePlayer> coowners, Bank bank, ChestLocation loc,
								 String name, BigDecimal balance, BigDecimal prevBalance, int multiplierStage,
								 int delayUntilNextPayout, int remainingOfflinePayouts, int remainingOfflineUntilReset) {
		return new Account(
				id,
				owner,
				new HashSet<>(coowners),
				bank,
				loc,
				name,
				QuickMath.scale(balance),
				QuickMath.scale(prevBalance),
				multiplierStage,
				delayUntilNextPayout,
				remainingOfflinePayouts,
				remainingOfflineUntilReset
		);
	}

	private boolean created;
	private boolean hasCustomName;

	private Bank bank;
	private ChestLocation chestLocation;
	private InventoryHolder inventoryHolder;

	private BigDecimal balance;
	private BigDecimal prevBalance;

	int multiplierStage;
	int delayUntilNextPayout;
	int remainingOfflinePayouts;
	int remainingOfflineUntilReset;

	private Account(int id, OfflinePlayer owner, Set<OfflinePlayer> coowners, Bank bank, ChestLocation loc,
					String name, BigDecimal balance, BigDecimal prevBalance, int multiplierStage,
					int delayUntilNextPayout, int remainingOfflinePayouts, int remainingOfflineUntilReset) {

		super(id, name, owner, coowners);
		this.bank = bank;
		this.chestLocation = loc;
		this.hasCustomName = !Objects.equals(getRawName(), getDefaultName());
		this.balance = balance;
		this.prevBalance = prevBalance;
		this.multiplierStage = multiplierStage;
		this.delayUntilNextPayout = delayUntilNextPayout;
		this.remainingOfflinePayouts = remainingOfflinePayouts;
		this.remainingOfflineUntilReset = remainingOfflineUntilReset;

	}

	/**
	 * Attempts to create this account. This method will ensure that the chest exists at
	 * the specified {@link Location} and is able to be opened.
	 *
	 * @return whether this account was successfully created
	 * @see Utils#isTransparent(Block)
	 */
	public boolean create() {

		if (created) {
			plugin.debugf("Account was already created! (#%d)", getID());
			return false;
		}
		plugin.debugf("Creating account (#%d)", getID());

		try {
			inventoryHolder = getChestLocation().findInventoryHolder();
			setChestLocation(ChestLocation.from(inventoryHolder));
			getChestLocation().checkSpaceAbove();
		} catch (ChestNotFoundException | ChestBlockedException e) {
			plugin.getAccountRepository().remove(this, Config.removeAccountOnError.get());
			if (!Config.removeAccountOnError.get())
				plugin.getAccountRepository().addInvalidAccount(this);

			plugin.getLogger().severe(e.getMessage());
			plugin.debug("Failed to create account (#" + getID() + ")");
			plugin.debug(e);
			return false;
		}

		final BigDecimal checkedBalance = calculateBalance();
		final int diff = checkedBalance.compareTo(getBalance());
		if (diff > 0) {
			if (getBalance().signum() == 0)
				plugin.debugf("Cool! Account #%d was created with a balance of %s already inside.",
						getID(), Utils.format(checkedBalance));
			else
				plugin.debugf("Value of account #%d was found higher than expected. Expected: %s but was: %s.",
						getID(), Utils.format(getBalance()), Utils.format(checkedBalance));
		} else if (diff < 0)
			plugin.debugf("Value of account #%d was found lower than expected. Expected: %s but was: %s.",
					getID(), Utils.format(getBalance()), Utils.format(checkedBalance));
		setBalance(checkedBalance);

		created = true;
		return true;
	}

	/**
	 * Gets the bank this account is registered at.
	 *
	 * @return the {@link Bank} of this account.
	 */
	public Bank getBank() {
		return bank;
	}

	/**
	 * Sets the bank of this account.
	 *
	 * @param bank the new {@link Bank} of this account.
	 */
	public void setBank(Bank bank) {
		getBank().removeAccount(this);
		getBank().notifyObservers();
		this.bank = bank;
		getBank().addAccount(this);
		notifyObservers();
		getBank().notifyObservers();
		plugin.getAccountRepository().notifyObservers();
	}

	/**
	 * Gets this account's current balance in {@link BigDecimal} format.
	 * The balance will always be positive.
	 *
	 * @return the current account balance
	 * @see #calculateBalance()
	 */
	public BigDecimal getBalance() {
		return balance;
	}

	/**
	 * Updates the current balance of this account.
	 * Called every time the account chest is <b>closed</b> and the value of the contents has changed.
	 *
	 * @param newBalance the new (positive) balance of the account.
	 */
	public void setBalance(BigDecimal newBalance) {
		if (newBalance == null || newBalance.signum() < 0)
			return;
		balance = QuickMath.scale(newBalance);
		notifyObservers();
		getBank().notifyObservers();
		plugin.getAccountRepository().notifyObservers();
	}

	/**
	 * Gets the balance of this account as it was at the previous interest payout.
	 *
	 * @return the previous account balance.
	 */
	public BigDecimal getPrevBalance() {
		return prevBalance;
	}

	/**
	 * Saves the current balance of this account into the previous balance.
	 * Used only at interest payout events.
	 *
	 * @see #calculateBalance()
	 * @see com.monst.bankingplugin.listeners.InterestEventListener
	 */
	public void updatePrevBalance() {
		prevBalance = balance;
	}

	/**
	 * Gets the location of this account.
	 *
	 * @return the {@link Location} of the account chest.
	 */
	public ChestLocation getChestLocation() {
		return chestLocation;
	}

	/**
	 * Sets the location of this account.
	 *
	 * @param chestLocation the new location
	 */
	public void setChestLocation(ChestLocation chestLocation) {
		this.chestLocation = chestLocation;
		notifyObservers();
	}

	/**
	 * Gets a nicer-looking description of the account's location.
	 *
	 * @return a {@link String} describing the location of the account chest.
	 */
	public String getCoordinates() {
		Location loc = chestLocation.getMinimumLocation();
		return loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
	}

	/**
	 * Gets the {@link Inventory} of this account chest.
	 *
	 * @return the account inventory.
	 */
	public InventoryHolder getInventoryHolder(boolean update) {
		if (!update)
			return inventoryHolder;
		try {
			return inventoryHolder = getChestLocation().findInventoryHolder();
		} catch (ChestNotFoundException e) {
			return null;
		}
	}

	/**
	 * @return 1 if single chest, 2 if double.
	 */
	public byte getSize() {
		return getChestLocation().getSize();
	}

	public boolean isSingleChest() {
		return getChestLocation().getSize() == 1;
	}

	public boolean isDoubleChest() {
		return getChestLocation().getSize() == 2;
	}

	public boolean hasCustomName() {
		return hasCustomName;
	}

	/**
	 * Sets the name of this account and updates the chest inventory screen to reflect the new name.
	 *
	 * @param name the new name of this account.
	 */
	@Override
	public void setName(String name) {
		this.name = name;
		hasCustomName = true;
		setChestName(getChestName());
	}

	public void resetName() {
		this.name = getDefaultName();
		hasCustomName = false;
		setChestName(getChestName());
	}

	private void setChestName(String name) {
		InventoryHolder ih = getInventoryHolder(true);
		if (ih == null)
			return;
		if (isDoubleChest()) {
			DoubleChest dc = (DoubleChest) ih;
			Chest left = (Chest) dc.getLeftSide();
			Chest right = (Chest) dc.getRightSide();
			if (left != null) {
				left.setCustomName(name);
				left.update();
			}
			if (right != null) {
				right.setCustomName(name);
				right.update();
			}
		} else {
			Chest chest = (Chest) ih;
			chest.setCustomName(name);
			chest.update();
		}
		notifyObservers();
		plugin.getAccountRepository().notifyObservers();
	}

	public String getChestName() {
		return hasCustomName ? getColorizedName() : getDefaultChestName();
	}

	public String getDefaultChestName() {
		return String.format(DEFAULT_CHEST_NAME, getOwner().getName(), getID());
	}

	/**
	 * Gets the default name of this account.
	 *
	 * @return the default name of this account.
	 */
	public String getDefaultName() {
		return String.format(DEFAULT_NAME, getOwner().getName());
	}

	/**
	 * Create a {@link Callback} that guarantees the current name of this account is
	 * correctly being reflected inside the chest inventory.
	 */
	public <T> Callback<T> callUpdateChestName() {
		return Callback.of(result -> setChestName(getChestName()));
	}

	/**
	 * Resets the name of the account inventory, e.g. "Chest" or "Large Chest"
	 */
	public void clearChestName() {
		setChestName("");
	}

	/**
	 * Calculates the value of the inventory contents of this account. This does not update the balance.
	 * @return the current value of the items inside this account's inventory
	 */
	public BigDecimal calculateBalance() {
		plugin.debugf("Appraising account... (#%d)", getID());
		InventoryHolder ih = getInventoryHolder(true);
		if (ih == null)
			return BigDecimal.ZERO;
		return plugin.getAccountRepository().appraise(ih.getInventory().getContents());
	}

	/**
	 * Gets the current multiplier stage of this account. This is an index on the list of multipliers specified by the bank.
	 * @return the current multiplier stage
	 */
	public int getMultiplierStage() {
		return multiplierStage;
	}

	/**
	 * Gets the interest delay at this account. This specifies how many interest cycles this account will skip before
	 * starting to generate interest (again).
	 *
	 * @return the current delay, in cycles
	 */
	public int getDelayUntilNextPayout() {
		return delayUntilNextPayout;
	}

	/**
	 * Gets the current number of remaining offline payouts at this account. This specifies how many (more) consecutive
	 * times this account will generate interest for offline account holders.
	 *
	 * @return the current number of remaining offline payouts
	 */
	public int getRemainingOfflinePayouts() {
		return remainingOfflinePayouts;
	}

	/**
	 * Gets the current number of remaining offline payouts until the multiplier resets at this account. This specifies
	 * how many (more) consecutive times this account will generate interest for offline account holders before the
	 * multiplier stage is reset to 0.
	 *
	 * @return the current number of remaining offline payouts until the multiplier is reset
	 */
	public int getRemainingOfflinePayoutsUntilReset() {
		return remainingOfflineUntilReset;
	}

	/**
	 * Determines whether to allow the next interest payout or not.
	 */
	public boolean allowNextPayout() {
		boolean online = isTrustedPlayerOnline();
		if (delayUntilNextPayout > 0) {
			if (online || bank.getCountInterestDelayOffline().get())
				delayUntilNextPayout--;
			return false;
		}
		if (online) {
			remainingOfflinePayouts = Math.max(remainingOfflinePayouts, bank.getAllowedOfflinePayouts().get());
			remainingOfflineUntilReset = Math.max(remainingOfflineUntilReset, bank.getAllowedOfflinePayoutsBeforeReset().get());
			return true;
		}
		return remainingOfflinePayouts-- > 0;
	}

	/**
	 * Processes a withdrawal at this account. This is only triggered when the balance of the account drops below
	 * the balance at the previous interest payout. The account multiplier is reduced by the amount specified
	 * at the bank.
	 *
	 * @return the new multiplier stage of this account
	 */
	public int processWithdrawal() {
		int decrement = bank.getWithdrawalMultiplierDecrement().get();
		if (decrement < 0) {
			return setMultiplierStage(0);
		}
		return setMultiplierStage(multiplierStage - decrement);
	}

	/**
	 * Increments this account's multiplier stage.
	 */
	public void incrementMultiplier() {
		if (isTrustedPlayerOnline())
			setMultiplierStage(++multiplierStage);
		else if (remainingOfflineUntilReset-- <= 0)
			setMultiplierStage(0);
		else
			setMultiplierStage(multiplierStage - bank.getOfflineMultiplierDecrement().get());
	}

	/**
	 * Gets the multiplier from Config:interestMultipliers corresponding to this account's current multiplier stage.
	 *
	 * @return the corresponding multiplier, or 1x by default in case of an error.
	 */
	public int getRealMultiplier() {
		List<Integer> multipliers = bank.getMultipliers().get();
		if (multipliers == null || multipliers.isEmpty())
			return 1;
		return multipliers.get(setMultiplierStage(multiplierStage));
	}

	/**
	 * Sets the multiplier stage. This will ensure that the provided stage is no less than 0 and no greater than multipliers.size() - 1.
	 *
	 * @param stage the stage to set the multiplier to
	 */
	public int setMultiplierStage(int stage) {
		if (stage == 0)
			return multiplierStage = 0;
		return multiplierStage = Math.max(0, Math.min(stage, bank.getMultipliers().get().size() - 1));
	}

	/**
	 * Sets the interest delay. This determines how long an account must wait before generating interest.
	 *
	 * @param delay the delay to set
	 */
	public void setDelayUntilNextPayout(int delay) {
		delayUntilNextPayout = Math.max(0, delay);
	}

	/**
	 * Sets the remaining offline payouts. This determines how many more times an account will be able to generate
	 * interest offline.
	 * @param remaining the number of payouts to allow
	 */
	public void setRemainingOfflinePayouts(int remaining) {
		remainingOfflinePayouts = Math.max(0, remaining);
	}

	/**
	 * Sets the remaining offline payouts until reset. This determines how many more times an account will be able to
	 * generate interest offline before the account multiplier resets.
	 * @param remaining the number of payouts to allow before the multiplier is reset
	 */
	public void setRemainingOfflinePayoutsUntilReset(int remaining) {
		remainingOfflineUntilReset = Math.max(0, remaining);
	}

	@Override
	public void setOwner(OfflinePlayer newOwner) {
		if (newOwner == null)
			return;
		OfflinePlayer previousOwner = getOwner();
		owner = newOwner;
		if (Config.trustOnTransfer.get())
			coowners.add(previousOwner);
		untrustPlayer(owner); // Remove from co-owners if new owner was a co-owner
		notifyObservers();
		plugin.getAccountRepository().notifyObservers();
	}

	@Override
	public String toConsolePrintout() {
		double interestR = getBank().getInterestRate().get();
		return Stream.of(
				"\"" + Utils.colorize(getRawName()) + ChatColor.GRAY + "\"",
				"Bank: " + ChatColor.RED + getBank().getColorizedName(),
				"Owner: " + ChatColor.GOLD + getOwnerDisplayName(),
				"Co-owners: " + Utils.map(getCoOwners(), OfflinePlayer::getName).toString(),
				"Balance: " + Utils.formatAndColorize(getBalance()),
				"Multiplier: " + ChatColor.AQUA + getRealMultiplier() + ChatColor.GRAY + " (Stage " + getMultiplierStage() + ")",
				"Interest rate: " + BigDecimal.valueOf(interestR * getRealMultiplier() * 100).setScale(1, BigDecimal.ROUND_HALF_EVEN) + "% " +
						ChatColor.GRAY + "(" + interestR + " x " + getRealMultiplier() + ")",
				"Location: " + ChatColor.AQUA + "(" + getCoordinates() + ")"
		).map(s -> ChatColor.GRAY + s).collect(Collectors.joining(", ", "", ""));
	}

	@Override
	public String toString() {
		return String.join(", ",
				"Account ID: " + getID(),
				"Owner: " + getOwner().getName(),
				"Bank: " + getBank().getName(),
				"Balance: " + Utils.format(getBalance()),
				"Location: " + getCoordinates()
		);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Account otherAccount = (Account) o;
		return getID() != -1 && Objects.equals(getID(), otherAccount.getID());
	}

	@Override
	public int hashCode() {
		return getID() != -1 ? getID() : Objects.hash(owner, coowners, bank, chestLocation, name);
	}
}
