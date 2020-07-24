package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.utils.AccountConfig;
import com.monst.bankingplugin.utils.AccountStatus;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.Slot.ClickHandler;
import org.ipvp.canvas.type.ChestMenu;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AccountGui extends Gui<Account> {

	static final Material ACCOUNT_BALANCE_BLOCK = Material.GOLD_INGOT;

	boolean isTrusted;

	public AccountGui(Account account) {
		super(BankingPlugin.getInstance(), account);
	}

	@Override
	Menu getMenu() {
		return ChestMenu.builder(1).title(guiSubject.getColorizedName()).build();
	}

	@Override
	void evaluateClearance(Player player) {
		isTrusted = guiSubject.isTrusted(player) || guiSubject.getBank().isTrusted(player)
				|| player.hasPermission(Permissions.ACCOUNT_INFO_OTHER);
	}

	@Override
	ItemStack createSlotItem(int i) {
		switch (i) {
			case 0:
				return createSlotItem(GENERAL_INFO_BLOCK, "Account Information", getGeneralInfoLore());
			case 1:
				return createSlotItem(GENERAL_INFO_BLOCK, "Bank Information", getBankInfoLore());
			case 3:
				if (isTrusted)
					return createSlotItem(ACCOUNT_BALANCE_BLOCK, "Account Standing", getBalanceLore());
				return createSlotItem(Material.BRICK, "Account Standing", Collections.singletonList("You do not have permission to view this."));
			case 4:
				if (isTrusted)
					return createSlotItem(MULTIPLIER_INFO_BLOCK, "Current Multiplier", Utils.getMultiplierLore(guiSubject));
				return createSlotItem(MULTIPLIER_INFO_BLOCK, "Multipliers", Utils.getMultiplierLore(guiSubject.getBank()));
			case 5:
				if (isTrusted)
					return createSlotItem(Material.IRON_BARS, "Account Restrictions", getAccountRestrictionsLore());
				return createSlotItem(Material.IRON_BARS, "Account Restrictions", Collections.singletonList("You do not have permission to view this."));
			case 8:
				if (isTrusted)
					return createSlotItem(Material.CHEST, "Account Contents", Collections.singletonList("Click to view account contents."));
				return createSlotItem(Material.CHEST, "Account Contents", Collections.singletonList("You do not have permission to view this."));
			default:
				return new ItemStack(Material.AIR);
		}
		// return new ItemStack(Material.AIR);
	}

	@Override
	ClickHandler createClickHandler(int i) {
		switch (i) {
			case 1:
				return (player, info) -> {
					new BankGui(guiSubject.getBank()).setPrevGui(this).open(player);
				};
			case 8:
				return (player, info) -> {
					if (isTrusted)
						new ChestMirrorGui(guiSubject).setPrevGui(this).open(player);
				};
			default:
				return (player, info) -> {
				};
		}
	}

	private List<String> getGeneralInfoLore() {
		return Arrays.asList(
				"ID: " + guiSubject.getID(),
				"Owner: " + ChatColor.GOLD + guiSubject.getOwnerDisplayName(),
				"Co-owners: " + (guiSubject.getCoowners().isEmpty() ? ChatColor.RED + "[none]"
						: ChatColor.AQUA + guiSubject.getCoowners().stream().map(OfflinePlayer::getName)
								.collect(Collectors.joining(", ", "[ ", " ]"))),
				"Location: " + ChatColor.AQUA + "(" + guiSubject.getCoordinates() + ")"
		);
	}

	private List<String> getBankInfoLore() {
		Bank bank = guiSubject.getBank();
		return Arrays.asList(
				"Name: \"" + ChatColor.RED + bank.getColorizedName() + ChatColor.GRAY + "\"",
				"Owner: " + (bank.isPlayerBank() ? ChatColor.GOLD + bank.getOwnerDisplayName() : ChatColor.RED + "ADMIN"),
				"Co-owners: " + (bank.getCoowners().isEmpty() ? org.bukkit.ChatColor.RED + "[none]"
						: ChatColor.AQUA + bank.getCoowners().stream().map(OfflinePlayer::getName)
						.collect(Collectors.joining(", ", "[ ", " ]"))),
				"Location: " + ChatColor.AQUA + bank.getSelection().getCoordinates(),
				"Click to view more info."
		);
	}

	private List<String> getBalanceLore() {
		AccountConfig config = guiSubject.getBank().getAccountConfig();
		boolean isLowBalance = guiSubject.getBalance().doubleValue() < config.getMinBalance(false);
		double interestRate = config.getInterestRate(false);
		int multiplier = guiSubject.getStatus().getRealMultiplier();
		double fullPayout = !(isLowBalance && !config.isPayOnLowBalance(false)) ? guiSubject.getBalance().doubleValue() * interestRate * multiplier : 0.0d;
		double lowBalanceFee = isLowBalance && config.getLowBalanceFee(false) > 0 ? config.getLowBalanceFee(false) : 0.0d;
		double nextPayout = fullPayout - lowBalanceFee;
		return Arrays.asList(
				"Balance: " + ChatColor.GREEN + "$" + Utils.formatNumber(guiSubject.getBalance()) + (isLowBalance ? ChatColor.RED + " (low)" : ""),
				"Interest rate: " + ChatColor.GREEN + (interestRate * multiplier * 100) + "% " + ChatColor.GRAY + "(" + interestRate + " x " + multiplier + ")",
				"Next payout: " + (nextPayout > 0 ? ChatColor.GREEN : ChatColor.RED) + "$" + Utils.formatNumber(nextPayout)
						+ (isLowBalance ? ChatColor.GRAY + " (" + ChatColor.GREEN + "$" + Utils.formatNumber(fullPayout)
						+ ChatColor.GRAY + " - " + ChatColor.RED + "$" + Utils.formatNumber(lowBalanceFee) + ChatColor.GRAY + ")" : "")
		);
	}

	private List<String> getAccountRestrictionsLore() {
		AccountStatus status = guiSubject.getStatus();
		int delay = status.getDelayUntilNextPayout();
		int remainingOffline = status.getRemainingOfflinePayouts();
		int untilReset = status.getRemainingOfflineUntilReset();
		int offlineDecrement = guiSubject.getBank().getAccountConfig().getOfflineMultiplierDecrement(false);
		return Arrays.asList(
				delay == 0
						? "Account will generate interest in the next payout cycle."
						: "Account will begin generating interest in " + ChatColor.AQUA + delay + ChatColor.GRAY + String.format(" payout cycle%s.", delay == 1 ? "" : "s"),
				"Account can generate interest for " + ChatColor.AQUA + remainingOffline + ChatColor.GRAY + String.format(" offline payout cycle%s.", remainingOffline == 1 ? "" : "s"),
				"Account multiplier will " + (untilReset < 0
						? "not reset while offline."
						: (untilReset == 0
								? "reset immediately on an offline payout."
								: "reset after " + ChatColor.AQUA + untilReset + ChatColor.GRAY + String.format(" offline payout cycle%s.", untilReset == 1 ? "" : "s"))),
				"Account multiplier will " + (offlineDecrement < 0
						? "decrease by " + ChatColor.AQUA + offlineDecrement + ChatColor.GRAY + " stages for every offline payout."
						: (offlineDecrement == 0
								? " freeze while offline."
								: " reset"))
		);
	}
}