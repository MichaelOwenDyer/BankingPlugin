package com.monst.bankingplugin.banking;

import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.exceptions.ChestBlockedException;
import com.monst.bankingplugin.exceptions.ChestNotFoundException;
import com.monst.bankingplugin.geo.locations.AccountLocation;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.QuickMath;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

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
	 * Opens a new account.
	 */
	public static Account open(Bank bank, OfflinePlayer owner, AccountLocation loc) {
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
				bank.getAllowedOfflinePayouts().get()
		);
	}

	/**
	 * Reopens an account that was stored in the database.
	 */
	public static Account reopen(int id, OfflinePlayer owner, Set<OfflinePlayer> coowners, Bank bank, AccountLocation loc,
								 String name, BigDecimal balance, BigDecimal prevBalance, int multiplierStage,
								 int delayUntilNextPayout, int remainingOfflinePayouts) {
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
				remainingOfflinePayouts
		);
	}

	private boolean created;
	private boolean hasCustomName;

	private Bank bank;
	private AccountLocation accountLocation;
	private InventoryHolder inventoryHolder;

	private BigDecimal balance;
	private BigDecimal prevBalance;

	int multiplierStage;
	int delayUntilNextPayout;
	int remainingOfflinePayouts;

	/**
	 * @param id the account ID {@link BankingEntity}
	 * @param owner the owner of the account {@link BankingEntity}
	 * @param coowners the co-owners of the account {@link BankingEntity}
	 * @param bank the {@link Bank} the account is registered at
	 * @param loc the {@link AccountLocation} of the account chest
	 * @param name the account name {@link Nameable}
	 * @param balance the current account balance {@link #getBalance()}
	 * @param prevBalance the previous account balance {@link #getPrevBalance()}
	 * @param multiplierStage the multiplier stage of this account
	 * @param delayUntilNextPayout the number of payments this account will wait before generating interest
	 * @param remainingOfflinePayouts the number of remaining offline interest payments this account will generate
	 */
	private Account(int id, OfflinePlayer owner, Set<OfflinePlayer> coowners, Bank bank, AccountLocation loc,
					String name, BigDecimal balance, BigDecimal prevBalance, int multiplierStage,
					int delayUntilNextPayout, int remainingOfflinePayouts) {

		super(id, name, owner, coowners);
		this.bank = bank;
		this.accountLocation = loc;
		this.hasCustomName = !Objects.equals(getRawName(), getDefaultName());
		this.balance = balance;
		this.prevBalance = prevBalance;
		this.multiplierStage = multiplierStage;
		this.delayUntilNextPayout = delayUntilNextPayout;
		this.remainingOfflinePayouts = remainingOfflinePayouts;

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
			inventoryHolder = getLocation().findInventoryHolder();
			accountLocation = AccountLocation.from(inventoryHolder);
			getLocation().checkSpaceAbove();
		} catch (ChestNotFoundException | ChestBlockedException e) {
			plugin.getAccountRepository().remove(this, Config.removeAccountOnError.get());
			if (!Config.removeAccountOnError.get())
				plugin.getAccountRepository().addMissingAccount(this);

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
	public AccountLocation getLocation() {
		return accountLocation;
	}

	/**
	 * Sets the location of this account.
	 *
	 * @param accountLocation the new location
	 */
	public void setLocation(AccountLocation accountLocation) {
		this.accountLocation = accountLocation;
		notifyObservers();
	}

	/**
	 * Gets a nicer-looking description of the account's location.
	 *
	 * @return a {@link String} describing the location of the account chest.
	 */
	public String getCoordinates() {
		return accountLocation.toString();
	}

	/**
	 * Gets the {@link InventoryHolder} of this account chest.
	 *
	 * @return the account inventory.
	 */
	public InventoryHolder getInventoryHolder(boolean update) {
		if (!update)
			return inventoryHolder;
		try {
			return inventoryHolder = getLocation().findInventoryHolder();
		} catch (ChestNotFoundException e) {
			return null;
		}
	}

	/**
	 * @return 1 if single chest, 2 if double.
	 */
	public byte getSize() {
		return getLocation().getSize();
	}

	public boolean isSingleChest() {
		return getLocation().getSize() == 1;
	}

	public boolean isDoubleChest() {
		return getLocation().getSize() == 2;
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
		BigDecimal sum = BigDecimal.ZERO;
		ItemStack[] contents = ih.getInventory().getContents();
		for (ItemStack item : contents) {
			if (Config.blacklist.contains(item))
				continue;
			BigDecimal itemValue = getWorth(item);
			if (item.getItemMeta() instanceof BlockStateMeta) {
				BlockStateMeta im = (BlockStateMeta) item.getItemMeta();
				if (im.getBlockState() instanceof ShulkerBox) {
					ShulkerBox shulkerBox = (ShulkerBox) im.getBlockState();
					for (ItemStack innerItem : shulkerBox.getInventory().getContents()) {
						if (Config.blacklist.contains(innerItem))
							continue;
						BigDecimal innerItemValue = getWorth(innerItem);
						if (innerItemValue.signum() != 0)
							innerItemValue = QuickMath.multiply(innerItemValue, innerItem.getAmount());
						itemValue = itemValue.add(innerItemValue);
					}
				}
			}
			if (itemValue.signum() != 0)
				itemValue = QuickMath.multiply(itemValue, item.getAmount());
			sum = sum.add(itemValue);
		}
		return QuickMath.scale(sum);
	}

	private BigDecimal getWorth(ItemStack item) {
		BigDecimal worth = plugin.getEssentials().getWorth().getPrice(plugin.getEssentials(), item);
		return worth != null ? worth : BigDecimal.ZERO;
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
		return getID() != -1 ? getID() : Objects.hash(owner, coowners, bank, accountLocation, name);
	}
}
