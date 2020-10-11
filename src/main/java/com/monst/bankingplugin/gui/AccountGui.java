package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.account.AccountStatus;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.banking.bank.BankField;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AccountGui extends SinglePageGui<Account> {

	boolean canTP;
	boolean isTrusted;

	public AccountGui(Account account) {
		super(account);
	}

	@Override
	void initializeMenu() {
		menu = ChestMenu.builder(1).title(guiSubject.getChestName()).build();
	}

	@Override
	void evaluateClearance(Player player) {
		canTP = player.isOp()
				|| player.hasPermission("minecraft.command.tp")
				|| player.hasPermission("essentials.tp.position");
		isTrusted = guiSubject.isTrusted(player) || guiSubject.getBank().isTrusted(player)
				|| player.hasPermission(Permissions.ACCOUNT_INFO_OTHER);
	}

	@Override
	ItemStack createSlotItem(int i) {
		switch (i) {
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
			case 8:
				if (isTrusted)
					return createSlotItem(Material.CHEST, "Account Contents", Collections.singletonList("Click to view account contents."));
				return createSlotItem(Material.CHEST, "Account Contents", NO_PERMISSION);
			default:
				return new ItemStack(Material.AIR);
		}
	}

	@Override
	ClickHandler createClickHandler(int i) {
		switch (i) {
			case 0:
				return canTP ? (player, info) -> {
					player.teleport(guiSubject.getLocation().setDirection(player.getLocation().getDirection()));
					this.close(player);
				} : null;
			case 1:
				return (player, info) -> new BankGui(guiSubject.getBank()).setPrevGui(this).open(player);
			case 8:
				return isTrusted ? (player, info) -> new AccountContentsGui(guiSubject).setPrevGui(this).open(player) : null;
			default:
				return null;
		}
	}

	@Override
	void setCloseHandler(Menu.CloseHandler handler) {
		menu.setCloseHandler(handler);
	}

	@Override
	GuiType getType() {
		return GuiType.ACCOUNT;
	}

	private List<String> getGeneralInfoLore() {
		List<String> lore = new ArrayList<>();
		lore.add("Account ID: " + guiSubject.getID());
		lore.add("Owner: " + ChatColor.GOLD + guiSubject.getOwnerDisplayName());
		lore.add("Co-owners: " + (guiSubject.getCoowners().isEmpty() ?
				ChatColor.RED + "[none]" :
				ChatColor.AQUA + Utils.map(guiSubject.getCoowners(), OfflinePlayer::getName).toString()));
		lore.add("Location: " + ChatColor.AQUA + "(" + guiSubject.getCoordinates() + ")");
		if (canTP)
			lore.add("Click to teleport to account.");
		return wordWrapAll(60, lore);
	}

	private List<String> getBankInfoLore() {
		Bank bank = guiSubject.getBank();
		return wordWrapAll(55,
				"Name: \"" + ChatColor.RED + bank.getColorizedName() + ChatColor.GRAY + "\"",
				"Owner: " + ChatColor.GOLD + bank.getOwnerDisplayName(),
				"Co-owners: " + (bank.getCoowners().isEmpty() ?
						ChatColor.RED + "[none]" :
						ChatColor.AQUA + Utils.map(bank.getCoowners(), OfflinePlayer::getName).toString()),
				"Click to view more info."
		);
	}

	private List<String> getBalanceLore() {
		Bank bank = guiSubject.getBank();
		double minBalance = bank.get(BankField.MINIMUM_BALANCE);
		boolean isLowBalance = guiSubject.getBalance().doubleValue() < minBalance;
		boolean payOnLowBalance = bank.get(BankField.PAY_ON_LOW_BALANCE);
		double interestRate = bank.get(BankField.INTEREST_RATE);
		int multiplier = guiSubject.getStatus().getRealMultiplier();
		double fullPayout = (isLowBalance && !payOnLowBalance) ?
				0.0 : guiSubject.getBalance().doubleValue() * interestRate * multiplier;
		double lowBalanceFee = isLowBalance && (double) bank.get(BankField.LOW_BALANCE_FEE) > 0 ?
				bank.get(BankField.LOW_BALANCE_FEE) : 0.0;
		double nextPayout = fullPayout - lowBalanceFee;
		return Arrays.asList(
				"Balance: " + ChatColor.GREEN + Utils.format(guiSubject.getBalance()) + (isLowBalance ?
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
		return getMultiplierLore(guiSubject.getBank().get(BankField.MULTIPLIERS), guiSubject.getStatus().getMultiplierStage());
	}

	private List<String> getAccountRestrictionsLore() {
		AccountStatus status = guiSubject.getStatus();
		int delay = status.getDelayUntilNextPayout();
		int remainingOffline = status.getRemainingOfflinePayouts();
		int untilReset = status.getRemainingOfflinePayoutsUntilReset();
		int offlineDecrement = guiSubject.getBank().get(BankField.OFFLINE_MULTIPLIER_DECREMENT);
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
}