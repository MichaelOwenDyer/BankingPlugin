package com.monst.bankingplugin.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.ClickOptions;
import org.ipvp.canvas.type.ChestMenu;
import org.ipvp.canvas.type.HopperMenu;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.utils.AccountConfig.Field;

import net.md_5.bungee.api.ChatColor;

public class GuiUtils {
	
	private static final Material GENERAL_INFO_BLOCK = Material.TOTEM_OF_UNDYING;
	private static final Material BALANCE_INFO_BLOCK = Material.EMERALD_BLOCK;
	private static final Material MULTIPLIER_INFO_BLOCK = Material.GOLD_BLOCK;

	public static Menu createGui(Account account, Player player) {

		Menu accountGui = HopperMenu.builder().title(account.getColorizedNickname()).build();

		final boolean isOwner = account.isOwner(player);
		final boolean verbose = account.isTrusted(player) || account.getBank().isTrusted(player)
				|| player.hasPermission(Permissions.ACCOUNT_INFO_OTHER_VERBOSE);
		
		accountGui.getSlot(0).setItem(createSlotItem(GENERAL_INFO_BLOCK, "Account Information", getGeneralInfoLore(account)));
		accountGui.getSlot(1).setItem(createSlotItem(BALANCE_INFO_BLOCK, "Account Standing", getBalanceLore(account)));
		accountGui.getSlot(2).setItem(createSlotItem(MULTIPLIER_INFO_BLOCK, "Multiplier", Arrays.asList("")));

		return accountGui;
	}

	private static List<String> getGeneralInfoLore(Account account) {
		return Arrays.asList("Bank: " + ChatColor.RED + account.getBank().getColorizedName(),
							"Owner: " + ChatColor.GOLD + account.getOwnerDisplayName(),
							"Co-owners: " + (account.getCoowners().isEmpty() ? ChatColor.RED + "[none]" 
									: ChatColor.AQUA + account.getCoowners().stream().map(OfflinePlayer::getName)
									.collect(Collectors.joining(", ", "[ ", " ]"))));
	}
	
	private static List<String> getBalanceLore(Account account) {
		return Arrays.asList("Balance: " + ChatColor.GREEN + "$" + Utils.formatNumber(account.getBalance()));
	}

	public static Menu createGui(Bank bank, Player player) {
		Menu accountGui = ChestMenu.builder(3).title(ChatColor.RED + bank.getColorizedName() + ChatColor.RED + " Management").build();

		final boolean isOwner = bank.isOwner(player);
		final boolean verbose = bank.isTrusted(player)
				|| (bank.isAdminBank() && player.hasPermission(Permissions.BANK_INFO_ADMIN)
						|| (!bank.isAdminBank() && player.hasPermission(Permissions.BANK_INFO_OTHER)));

		accountGui.getSlot(0).setItem(createSlotItem(GENERAL_INFO_BLOCK, "Bank Information", getGeneralInfoLore(bank)));
		accountGui.getSlot(1).setItem(createSlotItem(BALANCE_INFO_BLOCK, "Account Configuration", getConfigurationLore(bank)));
		accountGui.getSlot(2).setItem(createSlotItem(BALANCE_INFO_BLOCK, "Statistics", getStatisticsLore(bank)));

		makeAllClickable(accountGui);

		return accountGui;
	}

	private static List<String> getGeneralInfoLore(Bank bank) {
		List<String> info = new ArrayList<>();
		info.add("Owner: " + ChatColor.GOLD + bank.getOwnerDisplayName());
		if (!bank.getCoowners().isEmpty())
			info.add("Co-owners: " + ChatColor.AQUA + bank.getCoowners().stream().map(OfflinePlayer::getName)
							.collect(Collectors.joining(", ", "[ ", " ]")));
		info.add("Location: " + bank.getSelection().getCoordinates());
		return info;
	}

	private static List<String> getConfigurationLore(Bank bank) {
		AccountConfig config = bank.getAccountConfig();
		return Arrays.asList("Account creation fee: " + ChatColor.GREEN + "$" + Utils.formatNumber((double) config.getOrDefault(Field.ACCOUNT_CREATION_PRICE)),
							"Minimum balance: " + ChatColor.GREEN + "$" + Utils.formatNumber((double) config.getOrDefault(Field.MINIMUM_BALANCE)),
							"Low balance fee: " + ChatColor.RED + "$" + Utils.formatNumber((double) config.getOrDefault(Field.LOW_BALANCE_FEE)),
							"Equality score: " + ChatColor.GREEN + Utils.getGiniCoefficient(bank));
	}
	
	private static List<String> getStatisticsLore(Bank bank) {
		return Arrays.asList("Total value: " + ChatColor.GREEN + "$" + Utils.formatNumber(bank.getTotalValue()),
							"Average account value: " + ChatColor.GREEN + "$" + 
									Utils.formatNumber(!bank.getAccounts().isEmpty() 
											? bank.getTotalValue().doubleValue() / bank.getAccounts().size() 
											: bank.getAccounts().size()));
	}
	
	private static ItemStack createSlotItem(Material material, String displayName, List<String> lore) {
		ItemStack item = new ItemStack(material);
		ItemMeta itemMeta = item.getItemMeta();
		itemMeta.setDisplayName(ChatColor.GRAY + displayName);
		itemMeta.setLore(lore.stream().map(s -> ChatColor.GRAY + s).collect(Collectors.toList()));
		item.setItemMeta(itemMeta);
		return item;
	}

	private ClickOptions leftClickable() {
		return ClickOptions.builder().allow(org.bukkit.event.inventory.ClickType.LEFT).build();
	}

	private ClickOptions rightClickable() {
		return ClickOptions.builder().allow(org.bukkit.event.inventory.ClickType.RIGHT).build();
	}

	private static void makeAllClickable(Menu menu) {
		int slots = menu.getDimensions().getArea() - 1;
		for (int i = 0; i < slots; i++)
			menu.getSlot(i).setClickOptions(ClickOptions.ALLOW_ALL);
	}

}
