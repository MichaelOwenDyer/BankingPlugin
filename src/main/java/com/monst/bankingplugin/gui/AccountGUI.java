package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.util.Permission;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.Slot.ClickHandler;
import org.ipvp.canvas.type.ChestMenu;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.bukkit.ChatColor.*;

public class AccountGUI extends SinglePageGUI<Account> {

	boolean canTP;
	boolean isTrusted;

	public AccountGUI(BankingPlugin plugin, Account account) {
		super(plugin, account);
	}

	@Override
	Menu createMenu() {
		return ChestMenu.builder(1).title(guiSubject.getName()).build();
	}

	@Override
	void evaluateClearance(Player player) {
		canTP = player.isOp()
				|| player.hasPermission("minecraft.command.tp")
				|| player.hasPermission("essentials.tp.position");
		isTrusted = guiSubject.isTrusted(player)
				|| guiSubject.getBank().isTrusted(player)
				|| Permission.ACCOUNT_INFO_OTHER.ownedBy(player);
	}

	@Override
	ItemStack createSlotItem(int slot) {
		switch (slot) {
			case 0:
				return createSlotItem(guiSubject.getOwner(), "Account Information", getGeneralInfoLore());
			case 1:
				if (guiSubject.getBank().isPlayerBank())
					return createSlotItem(guiSubject.getBank().getOwner(), "Bank Information", getBankInfoLore());
				return createSlotItem(Material.PLAYER_HEAD, "Bank Information", getBankInfoLore());
			case 3:
				if (isTrusted)
					return createSlotItem(Material.GOLD_INGOT, "Account Standing", getBalanceLore());
				return createSlotItem(Material.GOLD_INGOT, "Account Standing", NO_PERMISSION);
			case 4:
				if (isTrusted)
					return createSlotItem(Material.NETHER_STAR, "Account Multiplier", getMultiplierLore());
				return createSlotItem(Material.NETHER_STAR, "Account Multiplier", NO_PERMISSION);
			case 5:
				if (isTrusted)
					return createSlotItem(Material.IRON_BARS, "Account Restrictions", getAccountRestrictionsLore());
				return createSlotItem(Material.IRON_BARS, "Account Restrictions", NO_PERMISSION);
			case 7:
				if (isTrusted)
					return createSlotItem(Material.BOOK, "Account History", getAccountHistoryLore());
				return createSlotItem(Material.BOOK, "Account History", NO_PERMISSION);
			case 8:
				if (isTrusted)
					return createSlotItem(Material.CHEST, "Account Contents", Collections.singletonList("Click to view account contents."));
				return createSlotItem(Material.CHEST, "Account Contents", NO_PERMISSION);
			default:
				return null;
		}
	}

	@Override
	ClickHandler createClickHandler(int slot) {
		switch (slot) {
			case 0:
				if (canTP)
					return (player, info) -> {
						teleport(player, guiSubject.getLocation().getTeleportLocation());
						exit(player);
					};
			case 1:
				return (player, info) -> new BankGUI(plugin, guiSubject.getBank()).setParentGUI(this).open(player);
			case 7:
				if (isTrusted)
					return (player, info) -> {
						if (info.getClickType().isLeftClick())
							new AccountTransactionGUI(plugin, guiSubject).setParentGUI(this).open(player);
						else if (info.getClickType().isRightClick())
							new AccountInterestGUI(plugin, guiSubject).setParentGUI(this).open(player);
					};
			case 8:
				if (isTrusted)
					return (player, info) -> {
						try {
							new AccountContentsGUI(plugin, guiSubject).setParentGUI(this).open(player);
						} catch (IllegalArgumentException e) {
							plugin.debug(e);
						}
					};
		}
		return null;
	}

	@Override
    GUIType getType() {
		return GUIType.ACCOUNT;
	}

	private List<String> getGeneralInfoLore() {
		Stream.Builder<String> lore = Stream.builder();
		lore.add("Account ID: " + guiSubject.getID());
		lore.add("Owner: " + GOLD + guiSubject.getOwner().getName());
		if (guiSubject.hasCoOwners())
			lore.add("Co-owners: " + AQUA + guiSubject.getCoOwners().stream()
					.map(OfflinePlayer::getName)
					.collect(Collectors.joining(", ")));
		lore.add("Location: " + AQUA + guiSubject.getCoordinates());
		if (canTP)
			lore.add("Click to teleport to account.");
		return wordWrapAll(60, lore.build());
	}

	private List<String> getBankInfoLore() {
		Stream.Builder<String> lore = Stream.builder();
		Bank bank = guiSubject.getBank();
		lore.add("Name: \"" + RED + bank.getColorizedName() + GRAY + "\"");
		if (bank.isPlayerBank())
			lore.add("Owner: " + GOLD + bank.getOwner().getName());
		if (bank.hasCoOwners())
			lore.add("Co-owners: " + AQUA + guiSubject.getCoOwners().stream()
					.map(OfflinePlayer::getName)
					.collect(Collectors.joining(", ")));
		lore.add("Click to view more info.");
		return wordWrapAll(55, lore.build());
	}

	private List<String> getBalanceLore() {
		Bank bank = guiSubject.getBank();
		BigDecimal interestRate = plugin.config().interestRate.at(bank);
		int multiplier = guiSubject.getInterestMultiplier(plugin.config().interestMultipliers.at(bank));
		BigDecimal multipliedInterestRate = interestRate.multiply(BigDecimal.valueOf(multiplier));
		BigDecimal minBalance = plugin.config().minimumBalance.at(bank);
		boolean isLowBalance = guiSubject.getBalance().compareTo(minBalance) < 0;
		boolean payOnLowBalance = plugin.config().payOnLowBalance.at(bank);
		BigDecimal fullPayout;
		if (isLowBalance && !payOnLowBalance)
			fullPayout = BigDecimal.ZERO;
		else
			fullPayout = guiSubject.getBalance().multiply(multipliedInterestRate).setScale(2, RoundingMode.HALF_EVEN);
		BigDecimal lowBalanceFee;
		if (isLowBalance)
			lowBalanceFee = plugin.config().lowBalanceFee.at(bank);
		else
			lowBalanceFee = BigDecimal.ZERO;
		BigDecimal nextPayout = fullPayout.subtract(lowBalanceFee);
		return Arrays.asList(
				"Balance: " + formatAndColorize(guiSubject.getBalance()) + (isLowBalance ?
						RED + " (" + format(minBalance.subtract(guiSubject.getBalance())) + " below minimum)" :
						""),
				"Interest rate: " + GREEN + multipliedInterestRate.scaleByPowerOfTen(2) + "% "
						+ GRAY + "(" + interestRate + " x " + multiplier + ")",
				"Next payout: " + formatAndColorize(nextPayout)
						+ (isLowBalance && payOnLowBalance ? GRAY + " (" + GREEN + format(fullPayout)
						+ GRAY + " - " + RED + format(lowBalanceFee) + GRAY + ")" : "")
		);
	}

	List<String> getMultiplierLore() {
		return getInterestMultiplierLore(plugin.config().interestMultipliers.at(guiSubject.getBank()), guiSubject.getInterestMultiplierStage());
	}

	private List<String> getAccountRestrictionsLore() {
		int remainingOffline = guiSubject.getRemainingOfflinePayouts();
		int offlineDecrement = plugin.config().offlineMultiplierDecrement.at(guiSubject.getBank());
		return wordWrapAll(
				"Account can generate interest for " + AQUA + remainingOffline + GRAY
						+ String.format(" offline payout cycle%s.", remainingOffline == 1 ? "" : "s"),
				"",
				"Account multiplier will " + (offlineDecrement == 0 ? " freeze while offline." :
						(offlineDecrement > 0 ?
								"decrease by " + AQUA + offlineDecrement + GRAY + " stages for every offline payout." :
								"reset upon the first offline payout."
						)
				)
		);

	}

	private List<String> getAccountHistoryLore() {
		return wordWrapAll(
				"Left click to view the transaction log.",
				"Right click to view the interest log."
		);
	}

}
