package com.monst.bankingplugin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.InventoryHolder;

import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.exceptions.ChestNotFoundException;
import com.monst.bankingplugin.exceptions.NotEnoughSpaceException;
import com.monst.bankingplugin.utils.AccountConfig.Field;
import com.monst.bankingplugin.utils.AccountStatus;
import com.monst.bankingplugin.utils.ItemUtils;
import com.monst.bankingplugin.utils.Ownable;
import com.monst.bankingplugin.utils.Utils;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

public class Account extends Ownable {

	private final BankingPlugin plugin;
	private boolean created;

	private int id;
	private String nickname;
	private final Location location;
	private final Bank bank;
	private InventoryHolder inventoryHolder;
	
	private final AccountStatus status;
	private BigDecimal balance;
	private BigDecimal prevBalance;

	public Account(BankingPlugin plugin, OfflinePlayer owner, Bank bank, Location loc) {
		this(-1, plugin, owner, null, bank, loc, new AccountStatus(bank.getAccountConfig()), null, BigDecimal.ZERO, BigDecimal.ZERO);
	}
	
	public Account(int id, BankingPlugin plugin, OfflinePlayer owner, Set<OfflinePlayer> coowners, Bank bank,
			Location loc, AccountStatus status, String nickname, BigDecimal balance, BigDecimal prevBalance) {
		this.id = id;
		this.plugin = plugin;
		this.owner = owner;
		this.coowners = coowners != null ? coowners : new HashSet<>();
		this.bank = bank;
		this.location = loc;
		this.status = status;
		this.nickname = nickname;
		this.balance = balance.setScale(2, RoundingMode.HALF_EVEN);
		this.prevBalance = prevBalance.setScale(2, RoundingMode.HALF_EVEN);
	}

	public boolean create(boolean showConsoleMessages) {
		if (created) {
			plugin.debug("Account was already created! (#" + id + ")");
			return false;
		}

		plugin.debug("Creating account (#" + id + ")");

		Block b = location.getBlock();
		if (!(b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST)) {
			
			ChestNotFoundException e = new ChestNotFoundException(
					String.format("No Chest found in world '%s' at location: %d; %d; %d", b.getWorld().getName(),
							b.getX(), b.getY(), b.getZ()));
			
			plugin.getAccountUtils().removeAccount(this, Config.removeAccountOnError);
			
			if (showConsoleMessages)
				plugin.getLogger().severe(e.getMessage());
			
			plugin.debug("Failed to create account (#" + id + ")");
			plugin.debug(e);
			return false;
			
		} else if (!ItemUtils.isTransparent(b.getRelative(BlockFace.UP))) {
			
			NotEnoughSpaceException e = new NotEnoughSpaceException(
					String.format("No space above chest in world '%s' at location: %d; %d; %d", b.getWorld().getName(),
							b.getX(), b.getY(), b.getZ()));
			
			plugin.getAccountUtils().removeAccount(this, Config.removeAccountOnError);
			
			if (showConsoleMessages)
				plugin.getLogger().severe(e.getMessage());
			
			plugin.debug("Failed to create account (#" + id + ")");
			plugin.debug(e);
			return false;
		}

		Chest chest = (Chest) b.getState();
		inventoryHolder = chest.getInventory().getHolder();

		BigDecimal checkedBalance = plugin.getAccountUtils().appraiseAccountContents(this);
		if (checkedBalance.compareTo(getBalance()) == 1) {
			if (getBalance().signum() <= 0) {
				plugin.debug("Cool! Account #" + id + " was created with a balance of " + checkedBalance.toString()
						+ " already inside.");
			} else {
				plugin.debug("Value of account #" + id + " was found higher than expected. Expected: $"
						+ getBalance().toString() + " but was: $" + checkedBalance);
			}
			setBalance(checkedBalance);

		} else if (checkedBalance.compareTo(getBalance()) == -1) {
			plugin.debug("Unexpected account value (#" + id + ")! Expected: $" + getBalance().toString() + " but was: $"
					+ checkedBalance.toString());
			if (plugin.getAccountUtils().payInsurance(this, getBalance().subtract(checkedBalance))) {
				plugin.debug("Insurance has been paid to account owner (#" + id + ")");
				setBalance(checkedBalance);
			} else {
				if (Config.insureAccountsUpTo < 0) {
					plugin.debug("There was an error while paying $" + getBalance().subtract(checkedBalance).toString()
							+ " in insurance.");
				} else if (Config.insureAccountsUpTo > 0) {
					double loss = getBalance().subtract(checkedBalance).doubleValue();
					double insurance = loss > Config.insureAccountsUpTo ? Config.insureAccountsUpTo : loss;
					plugin.debug("There was an error while paying the maximum $" + insurance + " in insurance.");
				}
			}
		}

		created = true;
		return true;
	}

	public boolean isCreated() {
		return created;
	}

	public void clearNickname() {
		setNickname("");
	}

	public String getDefaultNickname() {
		return ChatColor.DARK_GREEN + owner.getName() + "'s Account " + ChatColor.GRAY + "(#" + id + ")";
	}

	public void setDefaultNickname() {
		if (!hasID())
			return;
		setNickname(null);
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
		if (nickname == null)
			nickname = getDefaultNickname();
		if (isDoubleChest()) {
			DoubleChest dc = (DoubleChest) inventoryHolder;
			if (dc == null)
				return;
			Chest left = (Chest) dc.getLeftSide();
			Chest right = (Chest) dc.getRightSide();
			left.setCustomName(Utils.colorize(nickname));
			left.update();
			right.setCustomName(Utils.colorize(nickname));
			right.update();
		} else {
			Chest chest = (Chest) inventoryHolder;
			if (chest == null)
				return;
			chest.setCustomName(Utils.colorize(nickname));
			chest.update();
		}
	}

	public String getNickname() {
		return nickname;
	}

	public boolean hasNickname() {
		return nickname != null;
	}

	public AccountStatus getStatus() {
		return status;
	}
	
	public Bank getBank() {
		return bank;
	}

	public boolean hasID() {
		return id != -1;
	}

	public int getID() {
		return id;
	}
	
	public void setID(int id) {
		if (this.id == -1) {
			this.id = id;
		}
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public BigDecimal getPrevBalance() {
		return prevBalance;
	}
	
	/**
	 * Saves the current balance of this account into the previous balance. Used
	 * only at interest payout events. Should only be used AFTER refreshing the
	 * account balance with AccountUtils.appraiseAccountContents() to ensure the
	 * balance is fully up-to-date.
	 */
	public void updatePrevBalance() {
		prevBalance = balance;
	}

	/**
	 * Changes the current balance of this account. Used every time the account
	 * chest is accessed and the contents are changed.
	 * 
	 * @param newBalance the new (positive) balance of the account.
	 */
	public void setBalance(BigDecimal newBalance) {
		if (newBalance != null && newBalance.signum() >= 0)
			this.balance = newBalance.setScale(2, RoundingMode.HALF_EVEN);
	}

	public Location getLocation() {
		return location;
	}

	public InventoryHolder getInventoryHolder() {
		return inventoryHolder;
	}
	
	public short getChestSize() {
		return isDoubleChest() ? (short) 2 : 1;
	}

	public boolean isDoubleChest() {
		return inventoryHolder instanceof DoubleChest;
	}

	@SuppressWarnings("unchecked")
	public TextComponent getMultiplierView() {
		List<Integer> multipliers = (List<Integer>) bank.getAccountConfig().getOrDefault(Field.MULTIPLIERS);
		TextComponent message = new TextComponent();
		message.setColor(net.md_5.bungee.api.ChatColor.GRAY);

		if (multipliers.size() == 0) {
			message.setText(ChatColor.GREEN + "1x");
			return message;
		}

		List<List<Integer>> stackedMultipliers = new ArrayList<>();
		stackedMultipliers.add(new ArrayList<>());
		stackedMultipliers.get(0).add(multipliers.get(0));
		int level = 0;
		for (int i = 1; i < multipliers.size(); i++) {
			if (multipliers.get(i) == stackedMultipliers.get(level).get(0))
				stackedMultipliers.get(level).add(multipliers.get(i));
			else {
				stackedMultipliers.add(new ArrayList<>());
				stackedMultipliers.get(++level).add(multipliers.get(i));
			}
		}

		final int listSize = 5;
		int counter = status.getMultiplierStage();
		int stage = -1;
		for (List<Integer> list : stackedMultipliers) {
			stage++;
			if (counter - list.size() < 0)
				break;
			else
				counter -= list.size();
		}

		TextComponent openingBracket = new TextComponent(ChatColor.GOLD + " [");
		openingBracket.setBold(true);

		message.addExtra(openingBracket);

		TextComponent closingBracket = new TextComponent(ChatColor.GOLD + " ]");
		closingBracket.setBold(true);

		int lower = 0;
		int upper = stackedMultipliers.size();

		if (stackedMultipliers.size() > listSize) {
			lower = stage - (listSize / 2);
			upper = stage + (listSize / 2) + 1;
			while (lower < 0) {
				lower++;
				upper++;
			}
			while (upper > stackedMultipliers.size()) {
				lower--;
				upper--;
			}

			if (lower > 0)
				message.addExtra(" ...");
		}

		for (int i = lower; i < upper; i++) {
			TextComponent number = new TextComponent(" " + stackedMultipliers.get(i).get(0) + "x");

			if (i == stage) {
				number.setColor(net.md_5.bungee.api.ChatColor.GREEN);
				number.setBold(true);
			}
			int levelSize = stackedMultipliers.get(i).size();
			if (levelSize > 1) {
				ComponentBuilder cb = new ComponentBuilder();
				if (i < stage) {
					cb.append("" + ChatColor.GREEN + levelSize).append(ChatColor.DARK_GRAY + "/")
							.append("" + ChatColor.GREEN + levelSize);
				} else if (i > stage) {
					cb.append("0").color(net.md_5.bungee.api.ChatColor.RED).append("/")
							.color(net.md_5.bungee.api.ChatColor.DARK_GRAY).append("" + levelSize)
							.color(net.md_5.bungee.api.ChatColor.GREEN);
				} else {
					net.md_5.bungee.api.ChatColor color;
					if (counter == levelSize - 1)
						color = net.md_5.bungee.api.ChatColor.GREEN;
					else if (counter > (levelSize - 1) / 2)
						color = net.md_5.bungee.api.ChatColor.GOLD;
					else
						color = net.md_5.bungee.api.ChatColor.RED;
					
					cb.append("" + counter).color(color).append("/").color(net.md_5.bungee.api.ChatColor.DARK_GRAY)
							.append("" + levelSize).color(net.md_5.bungee.api.ChatColor.GREEN);
				}
				number.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, cb.create()));
			}
			message.addExtra(number);
		}
		if (upper < stackedMultipliers.size())
			message.addExtra(" ...");
		message.addExtra(closingBracket);
		return message;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Account otherAccount = (Account) o;
		return id != -1 && id == otherAccount.id;
	}

	@Override
	public int hashCode() {
		return id != -1 ? id : super.hashCode();
	}

	@Override
	public String toString() {
		String name;
		name = hasNickname() ? ChatColor.GRAY + "\"" + Utils.colorize(nickname) + ChatColor.GRAY + "\""
				: ChatColor.GRAY + "Account ID: " + ChatColor.WHITE + id;
		return name + "\n" + ChatColor.GRAY + "Owner: " + ChatColor.GOLD + getOwnerDisplayName() + "\n" + ChatColor.GRAY
				+ "Bank: " + ChatColor.AQUA + bank.getName() + ChatColor.GRAY;
	}

	public TextComponent toStringVerbose() {
		TextComponent info = new TextComponent(toString() + "\nBalance: ");
		info.setColor(net.md_5.bungee.api.ChatColor.GRAY);
		
		TextComponent message = new TextComponent(
				ChatColor.GREEN + "$" + Utils.formatNumber(getBalance()) + "\n");
		
		TextComponent multiplier = new TextComponent("Multiplier:");
		multiplier.addExtra(getMultiplierView());
		multiplier.addExtra("\nInterest rate: ");
		
		TextComponent interestRate = new TextComponent(ChatColor.GREEN + ""
				+ Math.round(((double) bank.getAccountConfig().getOrDefault(Field.INTEREST_RATE)
						* status.getRealMultiplier() * 100))
				+ "%" + ChatColor.GRAY + " (" + bank.getAccountConfig().getOrDefault(Field.INTEREST_RATE) + " x "
				+ status.getRealMultiplier() + ")\n");
		
		if (status.getRemainingUntilFirstPayout() != 0)
			interestRate.addExtra(new TextComponent(
					ChatColor.RED + " (" + status.getRemainingUntilFirstPayout() + " payouts to go)"));
		
		TextComponent loc = new TextComponent("Location: " + ChatColor.AQUA + "(" + location.getBlockX() + ", "
				+ location.getBlockY() + ", " + location.getBlockZ() + ")");
		
		info.addExtra(message);
		info.addExtra(multiplier);
		info.addExtra(interestRate);
		info.addExtra(loc);
		return info;
	}
}
