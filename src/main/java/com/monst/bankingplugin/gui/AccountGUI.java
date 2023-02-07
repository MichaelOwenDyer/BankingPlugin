package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.Permissions;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.lang.ColorStringBuilder;
import com.monst.bankingplugin.util.Observer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AccountGUI extends SinglePageGUI implements Observer {
	
	private final Account account;
	private boolean pendingUpdate;

	public AccountGUI(BankingPlugin plugin, Player player, Account account) {
		super(plugin, player);
		this.account = account;
		account.subscribe(this);
	}
	
	@Override
	public void onClose() {
		super.onClose();
		account.unsubscribe(this);
	}
	
	@Override
	public void update() {
		pendingUpdate = !visible;
		if (pendingUpdate)
			return;
		createItems(player).forEach(inventory::setItem);
	}
	
	@Override
	void reopen() {
		visible = true;
		if (pendingUpdate) {
			pendingUpdate = false;
			createItems(player).forEach(inventory::setItem);
		}
		player.openInventory(inventory);
	}
	
	Inventory createInventory() {
		return Bukkit.createInventory(this, 9, account.getName());
	}
	
	@Override
	Map<Integer, ItemStack> createItems(Player player) {
		boolean isTrusted = isTrusted(player);
		Map<Integer, ItemStack> items = new HashMap<>();
		items.put(0, head(account.getOwner(), "Account Information", getGeneralInfoLore(canTP(player))));
		items.put(1, head(account.getBank().getOwner(), "Bank Information", getBankInfoLore()));
		items.put(3, item(Material.GOLD_INGOT, "Account Standing", getBalanceLore(isTrusted)));
		items.put(4, item(Material.NETHER_STAR, "Account Multiplier", getMultiplierLore(isTrusted)));
		items.put(5, item(Material.IRON_BARS, "Account Restrictions", getAccountRestrictionsLore(isTrusted)));
		items.put(7, item(Material.BOOK, "Account History", getAccountHistoryLore(isTrusted)));
		items.put(8, item(Material.CHEST, "Account Contents", getAccountContentsLore(isTrusted)));
		return items;
	}
    
    private List<String> getGeneralInfoLore(boolean canTP) {
		Stream.Builder<String> lore = Stream.builder();
		lore.add("Account ID: " + account.getID());
		lore.add("Owner: " + ChatColor.GOLD + account.getOwner().getName());
		if (account.hasCoOwners())
			lore.add("Co-owners: " + ChatColor.AQUA + account.getCoOwners().stream()
					.map(OfflinePlayer::getName)
					.collect(Collectors.joining(", ")));
		lore.add("Location: " + ChatColor.AQUA + account.getCoordinates());
		if (canTP)
			lore.add("Click to teleport to account.");
		return wordWrapAll(60, lore.build());
	}
	
	private List<String> getBankInfoLore() {
		Stream.Builder<String> lore = Stream.builder();
		Bank bank = account.getBank();
		lore.add("Name: \"" + ChatColor.RED + bank.getColorizedName() + ChatColor.GRAY + "\"");
		if (bank.isPlayerBank())
			lore.add("Owner: " + ChatColor.GOLD + bank.getOwner().getName());
		if (bank.hasCoOwners())
			lore.add("Co-owners: " + ChatColor.AQUA + account.getCoOwners().stream()
					.map(OfflinePlayer::getName)
					.collect(Collectors.joining(", ")));
		lore.add("Click to view more info.");
		return wordWrapAll(55, lore.build());
	}
	
	private List<String> getBalanceLore(boolean isTrusted) {
		if (!isTrusted)
			return NO_PERMISSION;
		Bank bank = account.getBank();
		BigDecimal interestRate = plugin.config().interestRate.at(bank);
		int multiplier = account.getInterestMultiplier(plugin.config().interestMultipliers.at(bank));
		BigDecimal multipliedInterestRate = interestRate.multiply(BigDecimal.valueOf(multiplier));
		BigDecimal minBalance = plugin.config().minimumBalance.at(bank);
		boolean isLowBalance = account.getBalance().compareTo(minBalance) < 0;
		boolean payOnLowBalance = plugin.config().payOnLowBalance.at(bank);
		BigDecimal fullPayout;
		if (isLowBalance && !payOnLowBalance)
			fullPayout = BigDecimal.ZERO;
		else
			fullPayout = account.getBalance().multiply(multipliedInterestRate).setScale(2, RoundingMode.HALF_EVEN);
		BigDecimal lowBalanceFee;
		if (isLowBalance)
			lowBalanceFee = plugin.config().lowBalanceFee.at(bank);
		else
			lowBalanceFee = BigDecimal.ZERO;
		BigDecimal nextPayout = fullPayout.subtract(lowBalanceFee);
		return Arrays.asList(
				"Balance: " + formatAndColorize(account.getBalance()) + (isLowBalance ?
																		 ChatColor.RED + " (" + format(minBalance.subtract(
																				 account.getBalance())) + " below minimum)" :
																		 ""),
				"Interest rate: " + ChatColor.GREEN + multipliedInterestRate.scaleByPowerOfTen(2) + "% "
						+ ChatColor.GRAY + "(" + interestRate + " x " + multiplier + ")",
				"Next payout: " + formatAndColorize(nextPayout)
						+ (isLowBalance && payOnLowBalance ? ChatColor.GRAY + " (" + ChatColor.GREEN + format(fullPayout)
						+ ChatColor.GRAY + " - " + ChatColor.RED + format(lowBalanceFee) + ChatColor.GRAY + ")" : "")
		);
	}

	List<String> getMultiplierLore(boolean isTrusted) {
		if (!isTrusted)
			return NO_PERMISSION;
		List<Integer> interestMultipliers = plugin.config().interestMultipliers.at(account.getBank());
		if (interestMultipliers.isEmpty())
			return Collections.singletonList(ChatColor.GREEN + "1x");
		
		List<List<Integer>> collapsedMultipliers = new ArrayList<>();
		collapsedMultipliers.add(new ArrayList<>());
		collapsedMultipliers.get(0).add(interestMultipliers.get(0));
		int level = 0;
		for (int i = 1; i < interestMultipliers.size(); i++) {
			if (interestMultipliers.get(i).equals(collapsedMultipliers.get(level).get(0)))
				collapsedMultipliers.get(level).add(interestMultipliers.get(i));
			else {
				collapsedMultipliers.add(new ArrayList<>());
				collapsedMultipliers.get(++level).add(interestMultipliers.get(i));
			}
		}
		
		int highlightStage = account.getInterestMultiplierStage();
		int stage = -1;
		if (highlightStage != -1) {
			for (List<Integer> multiplierGroup : collapsedMultipliers) {
				stage++;
				if (highlightStage < multiplierGroup.size())
					break;
				highlightStage -= multiplierGroup.size();
			}
			highlightStage++;
		}
		
		List<String> lore = new ArrayList<>();
		
		final int listSize = 5;
		int lower = 0;
		int upper = collapsedMultipliers.size();
		
		if (stage != -1 && collapsedMultipliers.size() > listSize) {
			lower = stage - (listSize / 2);
			upper = stage + (listSize / 2) + 1;
			while (lower < 0) {
				lower++;
				upper++;
			}
			while (upper > collapsedMultipliers.size()) {
				lower--;
				upper--;
			}
			
			if (lower > 0)
				lore.add("...");
		}
		
		for (int i = lower; i < upper; i++) {
			ColorStringBuilder line = new ColorStringBuilder();
			if (i == stage)
				line.bold();
			
			line.gold(" - ", collapsedMultipliers.get(i).get(0), "x");
			
			int levelSize = collapsedMultipliers.get(i).size();
			if (levelSize > 1) {
				if (stage == -1) {
					line.darkGray(" (").gray("x").aqua(levelSize).darkGray(")");
				} else if (i < stage) {
					line.darkGray(" (").green(levelSize).darkGray("/").green(levelSize).darkGray(")");
				} else if (i > stage) {
					line.darkGray(" (").red("0").darkGray("/").green(levelSize).darkGray(")");
				} else {
					line.darkGray(" (");
					if (highlightStage * 3 >= levelSize * 2)
						line.green(highlightStage); // Over 2/3rds through the group
					else if (highlightStage * 3 >= levelSize)
						line.gold(highlightStage); // Between 1/3rd and 2/3rds through the group
					else
						line.red(highlightStage); // Below 1/3rd through the group
					line.darkGray("/").green(levelSize).darkGray(")");
				}
			}
			lore.add(line.toString());
		}
		if (upper < collapsedMultipliers.size())
			lore.add("...");
		return lore;
	}

	private List<String> getAccountRestrictionsLore(boolean isTrusted) {
		if (!isTrusted)
			return NO_PERMISSION;
		int remainingOffline = account.getRemainingOfflinePayouts();
		int offlineDecrement = plugin.config().offlineMultiplierDecrement.at(account.getBank());
		return wordWrapAll(
				"Account can generate interest for " + ChatColor.AQUA + remainingOffline + ChatColor.GRAY
						+ String.format(" offline payout cycle%s.", remainingOffline == 1 ? "" : "s"),
				"",
				"Account multiplier will " + (offlineDecrement == 0 ? " freeze while offline." :
						(offlineDecrement > 0 ?
								"decrease by " + ChatColor.AQUA + offlineDecrement + ChatColor.GRAY + " stages for every offline payout." :
								"reset upon the first offline payout."
						)
				)
		);
	}

	private List<String> getAccountHistoryLore(boolean isTrusted) {
		if (!isTrusted)
			return NO_PERMISSION;
		return wordWrapAll(
				"Left click to view the transaction log.",
				"Right click to view the interest log."
		);
	}
	
	private List<String> getAccountContentsLore(boolean isTrusted) {
		return isTrusted ? Collections.singletonList("Click to view account contents.") : NO_PERMISSION;
	}
	
	@Override
	public void click(int slot, ClickType type) {
		if (slot == 0)
			handleClickTeleport(player);
		else if (slot == 1)
			handleClickBankInfo();
		else if (slot == 7)
			handleClickAccountHistory(player, type);
		else if (slot == 8)
			handleClickAccountContents(player);
	}
	
	private void handleClickTeleport(Player player) {
		if (player.isOp()
				|| player.hasPermission("minecraft.command.tp")
				|| player.hasPermission("essentials.tp.position")) {
			teleport(player, account.getLocation().getTeleportLocation());
			exit();
		}
	}
	
	private void handleClickBankInfo() {
		child(new BankGUI(plugin, player, account.getBank())).open();
	}
	
	private void handleClickAccountHistory(Player player, ClickType click) {
		if (isTrusted(player)) {
			if (click.isLeftClick())
				child(new AccountTransactionGUI(plugin, player, account)).open();
			else if (click.isRightClick())
				child(new AccountInterestGUI(plugin, player, account)).open();
		}
	}
	
	private void handleClickAccountContents(Player player) {
		if (isTrusted(player))
			child(new AccountContentsGUI(plugin, player, account)).open();
	}
	
	private boolean canTP(Player p) {
		return p.isOp()
				|| p.hasPermission("minecraft.command.tp")
				|| p.hasPermission("essentials.tp.position");
	}
	
	private boolean isTrusted(Player p) {
		return account.isTrusted(p)
				|| account.getBank().isTrusted(p)
				|| Permissions.ACCOUNT_INFO_OTHER.ownedBy(p);
	}

}
