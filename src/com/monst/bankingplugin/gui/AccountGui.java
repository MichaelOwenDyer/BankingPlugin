package com.monst.bankingplugin.gui;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.Slot.ClickHandler;
import org.ipvp.canvas.type.HopperMenu;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;

import net.md_5.bungee.api.ChatColor;

public class AccountGui extends Gui<Account> {

	static final Material ACCOUNT_BALANCE_BLOCK = Material.GOLD_INGOT;
	
	public AccountGui(Account account) {
		this.guiSubject = account;
	}

	@Override
	Menu getMenu() {
		return HopperMenu.builder().title(guiSubject.getColorizedNickname()).build();
	}

	@Override
	boolean getClearance(Player player) {
		return guiSubject.isTrusted(player) || guiSubject.getBank().isTrusted(player)
				|| player.hasPermission(Permissions.ACCOUNT_INFO_OTHER_VERBOSE);
	}

	@Override
	ItemStack createSlotItem(int i) {
		switch (i) {
		case 0:
			return createSlotItem(GENERAL_INFO_BLOCK, "Account Information", getGeneralInfoLore());
		case 2:
			return createSlotItem(ACCOUNT_BALANCE_BLOCK, "Account Standing", getBalanceLore());
		case 4:
			return createSlotItem(MULTIPLIER_INFO_BLOCK, "Multiplier", Arrays.asList(""));
		default:
			return new ItemStack(Material.AIR);
		}
	}

	@Override
	ClickHandler createClickHandler(int i) {
		switch (i) {
		case 0:

		default:
			return (player, info) -> {
			};
		}
	}

	private List<String> getGeneralInfoLore() {
		return Arrays.asList("Bank: " + ChatColor.RED + guiSubject.getBank().getColorizedName(),
				"Owner: " + ChatColor.GOLD + guiSubject.getOwnerDisplayName(),
				"Co-owners: " + (guiSubject.getCoowners().isEmpty() ? ChatColor.RED + "[none]"
						: ChatColor.AQUA + guiSubject.getCoowners().stream().map(OfflinePlayer::getName)
								.collect(Collectors.joining(", ", "[ ", " ]"))));
	}

	private List<String> getBalanceLore() {
		return Arrays.asList("Balance: " + ChatColor.GREEN + "$" + Utils.formatNumber(guiSubject.getBalance()));
	}

}
