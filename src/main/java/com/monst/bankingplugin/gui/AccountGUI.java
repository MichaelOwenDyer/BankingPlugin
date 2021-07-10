package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.banking.Account;
import com.monst.bankingplugin.banking.Bank;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class AccountGUI extends SinglePageGUI<Account> {

	boolean canTP;
	boolean isTrusted;

	public AccountGUI(Account account) {
		super(account);
	}

	@Override
	Menu createMenu() {
		return ChestMenu.builder(1).title(guiSubject.getChestName()).build();
	}

	@Override
	void evaluateClearance(Player player) {
		canTP = player.isOp()
				|| Permissions.hasAny(player, "minecraft.command.tp", "essentials.tp.position");
		isTrusted = guiSubject.isTrusted(player)
				|| guiSubject.getBank().isTrusted(player)
				|| player.hasPermission(Permissions.ACCOUNT_INFO_OTHER);
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
				return new ItemStack(Material.AIR);
		}
	}

	@Override
	ClickHandler createClickHandler(int slot) {
		switch (slot) {
			case 0:
				if (canTP)
					return (player, info) -> {
						Utils.teleport(player, guiSubject.getLocation().getTeleportLocation());
						exit(player);
					};
			case 1:
				return (player, info) -> new BankGUI(guiSubject.getBank()).setParentGUI(this).open(player);
			case 7:
				if (isTrusted)
					return (player, info) -> {
						if (info.getClickType().isLeftClick())
							new AccountTransactionGUI(guiSubject).setParentGUI(this).open(player);
						else if (info.getClickType().isRightClick())
							new AccountInterestGUI(guiSubject).setParentGUI(this).open(player);
					};
			case 8:
				if (isTrusted)
					return (player, info) -> new AccountContentsGUI(guiSubject).setParentGUI(this).open(player);
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
		lore.add("Owner: " + ChatColor.GOLD + guiSubject.getOwnerDisplayName());
		lore.add("Co-owners: " + (guiSubject.getCoOwners().isEmpty() ?
				ChatColor.RED + "[none]" :
				ChatColor.AQUA + Utils.map(guiSubject.getCoOwners(), OfflinePlayer::getName).toString()));
		lore.add("Location: " + ChatColor.AQUA + "(" + guiSubject.getCoordinates() + ")");
		if (canTP)
			lore.add("Click to teleport to account.");
		return wordWrapAll(60, lore.build());
	}

	private List<String> getBankInfoLore() {
		Bank bank = guiSubject.getBank();
		return wordWrapAll(55,
				"Name: \"" + ChatColor.RED + bank.getColorizedName() + ChatColor.GRAY + "\"",
				"Owner: " + ChatColor.GOLD + bank.getOwnerDisplayName(),
				"Co-owners: " + (bank.getCoOwners().isEmpty() ?
						ChatColor.RED + "[none]" :
						ChatColor.AQUA + Utils.map(bank.getCoOwners(), OfflinePlayer::getName).toString()),
				"Click to view more info."
		);
	}

	private List<String> getBalanceLore() {
		Bank bank = guiSubject.getBank();
		double minBalance = bank.getMinimumBalance().get();
		boolean isLowBalance = guiSubject.getBalance().doubleValue() < minBalance;
		boolean payOnLowBalance = bank.getPayOnLowBalance().get();
		double interestRate = bank.getInterestRate().get();
		int multiplier = guiSubject.getRealMultiplier();
		double fullPayout = (isLowBalance && !payOnLowBalance) ?
				0.0 : guiSubject.getBalance().doubleValue() * interestRate * multiplier;
		double lowBalanceFee = isLowBalance && bank.getLowBalanceFee().get() > 0 ?
				bank.getLowBalanceFee().get() : 0.0;
		double nextPayout = fullPayout - lowBalanceFee;
		return Arrays.asList(
				"Balance: " + Utils.formatAndColorize(guiSubject.getBalance()) + (isLowBalance ?
						ChatColor.RED + " (" + Utils.format(minBalance - guiSubject.getBalance().doubleValue()) + " below minimum)" :
						""),
				"Interest rate: " + ChatColor.GREEN + BigDecimal.valueOf(interestRate * multiplier * 100).setScale(1, BigDecimal.ROUND_HALF_EVEN)
						+ "% " + ChatColor.GRAY + "(" + interestRate + " x " + multiplier + ")",
				"Next payout: " + (nextPayout > 0 ? ChatColor.GREEN : ChatColor.RED) + Utils.format(nextPayout)
						+ (isLowBalance && payOnLowBalance ? ChatColor.GRAY + " (" + ChatColor.GREEN + Utils.format(fullPayout)
						+ ChatColor.GRAY + " - " + ChatColor.RED + Utils.format(lowBalanceFee) + ChatColor.GRAY + ")" : "")
		);
	}

	List<String> getMultiplierLore() {
		return getMultiplierLore(guiSubject.getBank().getMultipliers().get(), guiSubject.getMultiplierStage());
	}

	private List<String> getAccountRestrictionsLore() {
		int delay = guiSubject.getDelayUntilNextPayout();
		int remainingOffline = guiSubject.getRemainingOfflinePayouts();
		int untilReset = guiSubject.getRemainingOfflinePayoutsUntilReset();
		int offlineDecrement = guiSubject.getBank().getOfflineMultiplierDecrement().get();
		return wordWrapAll(
				(delay == 0 ?
						"This account will generate interest in the next payout cycle." :
						"This account will begin generating interest in " + ChatColor.AQUA + delay + ChatColor.GRAY
								+ String.format(" payout cycle%s.", delay == 1 ? "" : "s")),
				"",
				"Account can generate interest for " + ChatColor.AQUA + remainingOffline + ChatColor.GRAY
						+ String.format(" offline payout cycle%s.", remainingOffline == 1 ? "" : "s"),
				"",
				"Account multiplier will " + (untilReset < 0 ?
						"not reset while offline." :
						(untilReset == 0 ?
								"reset immediately on an offline payout." :
								"reset after " + ChatColor.AQUA + untilReset + ChatColor.GRAY
										+ String.format(" offline payout cycle%s.", untilReset == 1 ? "" : "s"))),
				"",
				"Account multiplier will " + (offlineDecrement < 0 ?
						"decrease by " + ChatColor.AQUA + offlineDecrement + ChatColor.GRAY + " stages for every offline payout." :
						(offlineDecrement == 0 ?
								" freeze while offline." : " reset"))
		);

	}

	private List<String> getAccountHistoryLore() {
		return wordWrapAll(
				"Left click to view the transaction log.",
				"Right click to view the interest log."
		);
	}

}
