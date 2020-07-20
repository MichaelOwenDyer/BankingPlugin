package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.utils.AccountConfig;
import com.monst.bankingplugin.utils.AccountConfig.Field;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.Slot.ClickHandler;
import org.ipvp.canvas.type.ChestMenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BankGui extends Gui<Bank> {

	public BankGui(Bank bank) {
		guiSubject = bank;
	}

	@Override
	Menu getMenu() {
		return ChestMenu.builder(2).title(ChatColor.RED + guiSubject.getColorizedName() + ChatColor.RED + " Management").build();
	}

	@Override
	boolean getClearance(Player player) {
		return guiSubject.isTrusted(player)
				|| (guiSubject.isAdminBank() && player.hasPermission(Permissions.BANK_INFO_ADMIN)
				|| (! guiSubject.isAdminBank() && player.hasPermission(Permissions.BANK_INFO_OTHER)));
	}

	@Override
	ItemStack createSlotItem(int i) {
		switch (i) {
			case 0:
				return createSlotItem(GENERAL_INFO_BLOCK, "Bank Information", getGeneralInfoLore());
			case 8:
				return createSlotItem(Material.CAKE, "Statistics", getStatisticsLore());
			case 9:
				return createSlotItem(Material.CHEST, "Account Creation", getCreationLore());
			case 10:
				return createSlotItem(MULTIPLIER_INFO_BLOCK, "Multipliers", Utils.getMultiplierLore(guiSubject));
			default:
				return new ItemStack(Material.AIR);
		}
	}

	@Override
	ClickHandler createClickHandler(int i) {
		switch (i) {
			case 0:
				return (player, info) -> {
					player.sendMessage("Nice!");
					player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.MASTER, 1.0f, 1.0f);
				};
			default:
				return (player, info) -> {
				};
		}
	}

	private List<String> getGeneralInfoLore() {
		List<String> info = new ArrayList<>();
		info.add("Owner: " + (guiSubject.isPlayerBank() ? ChatColor.GOLD + guiSubject.getOwnerDisplayName() : ChatColor.RED + "ADMIN"));
		if (!guiSubject.getCoowners().isEmpty())
			info.add("Co-owners: " + ChatColor.AQUA + guiSubject.getCoowners().stream().map(OfflinePlayer::getName)
					.collect(Collectors.joining(", ", "[ ", " ]")));
		info.add("Location: " + ChatColor.AQUA + guiSubject.getSelection().getCoordinates());
		return info;
	}

	private List<String> getStatisticsLore() {
		return Arrays.asList("Total value: " + ChatColor.GREEN + "$" + Utils.formatNumber(guiSubject.getTotalValue()),
				"Average account value: " + ChatColor.GREEN + "$" +
						Utils.formatNumber(! guiSubject.getAccounts().isEmpty()
								? guiSubject.getTotalValue().doubleValue() / guiSubject.getAccounts().size()
								: guiSubject.getAccounts().size()),
				"Equality score: " + Utils.getGiniLore(guiSubject));
	}

	private List<String> getCreationLore() {
		AccountConfig config = guiSubject.getAccountConfig();
		List<String> lore = new ArrayList<>();
		lore.add("Fee: " + ChatColor.GREEN + "$" + Utils.formatNumber((double) config.getOrDefault(Field.ACCOUNT_CREATION_PRICE)));
		boolean reimburse = ((boolean) config.getOrDefault(Field.REIMBURSE_ACCOUNT_CREATION));
		lore.add("Fee Reimbursement: " + (reimburse ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
		return lore;
	}

	private List<String> getConfigurationLore() {
		AccountConfig config = guiSubject.getAccountConfig();
		return Arrays.asList(
				"Account creation fee: " + ChatColor.GREEN + "$"
						+ Utils.formatNumber((double) config.getOrDefault(Field.ACCOUNT_CREATION_PRICE)),
				"Minimum balance: " + ChatColor.GREEN + "$"
						+ Utils.formatNumber((double) config.getOrDefault(Field.MINIMUM_BALANCE)),
				"Low balance fee: " + ChatColor.RED + "$"
						+ Utils.formatNumber((double) config.getOrDefault(Field.LOW_BALANCE_FEE)));
	}
}
