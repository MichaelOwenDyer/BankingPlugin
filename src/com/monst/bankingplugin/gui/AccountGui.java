package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.utils.AccountConfig;
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

	public AccountGui(Account account) {
		super(account);
	}

	@Override
	Menu getMenu() {
		return ChestMenu.builder(1).title(guiSubject.getColorizedName()).build();
	}

	@Override
	void getClearance(Player player) {
		highClearance = guiSubject.isTrusted(player) || guiSubject.getBank().isTrusted(player)
				|| player.hasPermission(Permissions.ACCOUNT_INFO_OTHER);
	}

	@Override
	ItemStack createSlotItem(int i) {
		switch (i) {
			case 0:
				return createSlotItem(GENERAL_INFO_BLOCK, "Account Information", getGeneralInfoLore());
			case 1:
				return createSlotItem(GENERAL_INFO_BLOCK, "Bank Information", getBankInfoLore());
			case 2:
				if (highClearance)
					return createSlotItem(ACCOUNT_BALANCE_BLOCK, "Account Standing", getBalanceLore());
				break;
			case 6:
				return createSlotItem(MULTIPLIER_INFO_BLOCK, "Multiplier", Utils.getMultiplierLore(guiSubject));
			case 8:
				if (highClearance)
					return createSlotItem(Material.CHEST, "Account Contents", Collections.singletonList("Click to view account contents."));
				break;
			default:
				return new ItemStack(Material.AIR);
		}
		return new ItemStack(Material.AIR);
	}

	@Override
	ClickHandler createClickHandler(int i) {
		switch (i) {
			case 1:
				return (player, info) -> {
					new BankGui(guiSubject.getBank()).setPrevGui(this).open(player);
				};
			case 3:
				return (player, info) -> {
					player.sendMessage("Not implemented yet!");
				};
			case 8:
				return (player, info) -> {
					if (highClearance)
						new ChestMirrorGui(guiSubject).setPrevGui(this).open(player);
				};
			default:
				return (player, info) -> {
				};
		}
	}

	private List<String> getGeneralInfoLore() {
		return Arrays.asList(
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
				"Location: " + ChatColor.AQUA + bank.getSelection().getCoordinates()
		);
	}

	private List<String> getBalanceLore() {
		AccountConfig config = guiSubject.getBank().getAccountConfig();
		double interestRate = config.getInterestRate(false);
		int multiplier = guiSubject.getStatus().getRealMultiplier();
		return Arrays.asList(
				"Balance: " + ChatColor.GREEN + "$" + Utils.formatNumber(guiSubject.getBalance()),
				"Interest rate: " + ChatColor.GREEN + (interestRate * multiplier * 100) + "% "
						+ ChatColor.GRAY + "(" + interestRate + " x " + multiplier + ")",
				"Next payout: " + ChatColor.GREEN + "$" + Utils.formatNumber(guiSubject.getBalance().doubleValue() * interestRate * multiplier)
		);
	}
}