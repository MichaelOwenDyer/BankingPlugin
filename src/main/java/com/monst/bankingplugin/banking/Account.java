package com.monst.bankingplugin.banking;

import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.exceptions.ChestNotFoundException;
import com.monst.bankingplugin.geo.locations.AccountLocation;
import com.monst.bankingplugin.utils.Callback;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
				0,
				bank.initialInterestDelay().get(),
				bank.allowedOfflinePayouts().get()
		);
	}

	/**
	 * Reopens an account that was stored in the database.
	 */
	public static Account reopen(int id, OfflinePlayer owner, Set<OfflinePlayer> coowners, Bank bank,
								 AccountLocation location, String name, BigDecimal previousBalance,
								 int multiplierStage, int delayUntilNextPayout, int remainingOfflinePayouts) {
		return new Account(
				id,
				owner,
				new HashSet<>(coowners),
				bank,
				location,
				name,
				previousBalance.setScale(2, RoundingMode.HALF_EVEN),
				multiplierStage,
				delayUntilNextPayout,
				remainingOfflinePayouts
		);
	}

	private Bank bank;
	private AccountLocation accountLocation;

	private BigDecimal balance;
	private BigDecimal previousBalance;

	private boolean hasCustomName;

	int multiplierStage;
	int delayUntilNextPayout;
	int remainingOfflinePayouts;

	/**
	 * @param id the account ID
	 * @param owner the owner of the account
	 * @param coowners the co-owners of the account
	 * @param bank the {@link Bank} the account is registered at
	 * @param loc the {@link AccountLocation} of the account chest
	 * @param name the account name
	 * @param previousBalance the previous account balance
	 * @param multiplierStage the multiplier stage of this account
	 * @param delayUntilNextPayout the number of payments this account will wait before generating interest
	 * @param remainingOfflinePayouts the number of remaining offline interest payments this account will generate
	 */
	private Account(int id, OfflinePlayer owner, Set<OfflinePlayer> coowners, Bank bank, AccountLocation loc,
					String name, BigDecimal previousBalance, int multiplierStage,
					int delayUntilNextPayout, int remainingOfflinePayouts) {

		super(id, name, owner, coowners);
		this.bank = bank;
		this.accountLocation = loc;
		this.previousBalance = previousBalance;
		this.hasCustomName = !Objects.equals(getRawName(), getDefaultName());
		this.multiplierStage = multiplierStage;
		this.delayUntilNextPayout = delayUntilNextPayout;
		this.remainingOfflinePayouts = remainingOfflinePayouts;

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
		this.bank = bank;
		getBank().addAccount(this);
		notifyObservers();
	}

	/**
	 * Gets this account's current balance in {@link BigDecimal} format.
	 * The balance will always be positive.
	 *
	 * @return the current account balance
	 * @see #calculateBalance()
	 */
	public BigDecimal getBalance() {
		if (balance == null)
			reloadBalance();
		return balance;
	}

	/**
	 * Updates the current balance of this account.
	 * Called every time the account chest is <b>closed</b>.
	 */
	public void reloadBalance() {
		balance = calculateBalance();
		notifyObservers();
		getBank().notifyObservers();
		plugin.getAccountRepository().getAccountMap().notifyObservers();
	}

	/**
	 * Gets the balance of this account as it was at the previous interest payout.
	 *
	 * @return the previous account balance.
	 */
	public BigDecimal getPreviousBalance() {
		return previousBalance;
	}

	/**
	 * Saves the current balance of this account into the previous balance.
	 * Used only at interest payout events.
	 *
	 * @see com.monst.bankingplugin.listeners.InterestEventListener
	 */
	public void updatePreviousBalance() {
		previousBalance = balance;
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
		InventoryHolder ih;
		try {
			ih = getLocation().findChest();
		} catch (ChestNotFoundException e) {
			return;
		}
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
		InventoryHolder ih;
		BigDecimal sum = BigDecimal.ZERO;
		try {
			ih = getLocation().findChest();
		} catch (ChestNotFoundException e) {
			return sum;
		}
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
							innerItemValue = innerItemValue.multiply(BigDecimal.valueOf(innerItem.getAmount()));
						itemValue = itemValue.add(innerItemValue);
					}
				}
			}
			if (itemValue.signum() != 0)
				itemValue = itemValue.multiply(BigDecimal.valueOf(item.getAmount()));
			sum = sum.add(itemValue);
		}
		return sum.setScale(2, RoundingMode.HALF_EVEN);
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
			if (online || bank.countInterestDelayOffline().get())
				delayUntilNextPayout--;
			return false;
		}
		if (online) {
			remainingOfflinePayouts = Math.max(remainingOfflinePayouts, bank.allowedOfflinePayouts().get());
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
		int decrement = bank.withdrawalMultiplierDecrement().get();
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
			setMultiplierStage(multiplierStage - bank.offlineMultiplierDecrement().get());
	}

	/**
	 * Gets the multiplier from Config:interestMultipliers corresponding to this account's current multiplier stage.
	 *
	 * @return the corresponding multiplier, or 1x by default in case of an error.
	 */
	public int getRealMultiplier() {
		List<Integer> multipliers = bank.multipliers().get();
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
		return multiplierStage = Math.max(0, Math.min(stage, bank.multipliers().get().size() - 1));
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
	}

	@Override
	public String toConsolePrintout() {
		BigDecimal rate = getBank().interestRate().get();
		int multiplier = getRealMultiplier();
		BigDecimal percentage = rate.multiply(BigDecimal.valueOf(multiplier)).scaleByPowerOfTen(2).setScale(2, RoundingMode.HALF_EVEN);
		return Stream.of(
				"\"" + Utils.colorize(getRawName()) + ChatColor.GRAY + "\"",
				"Bank: " + ChatColor.RED + getBank().getColorizedName(),
				"Owner: " + ChatColor.GOLD + getOwnerDisplayName(),
				"Co-owners: " + Utils.map(getCoOwners(), OfflinePlayer::getName).toString(),
				"Balance: " + Utils.formatAndColorize(getBalance()),
				"Multiplier: " + ChatColor.AQUA + getRealMultiplier() + ChatColor.GRAY + " (Stage " + getMultiplierStage() + ")",
				"Interest rate: " + percentage + "% " + ChatColor.GRAY + "(" + rate + " x " + multiplier + ")",
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
