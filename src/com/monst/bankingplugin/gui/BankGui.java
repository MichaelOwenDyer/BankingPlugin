package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.utils.AccountConfig;
import com.monst.bankingplugin.utils.Permissions;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.Slot.ClickHandler;
import org.ipvp.canvas.type.ChestMenu;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BankGui extends Gui<Bank> {

	private static final Material DEFAULT_BLOCK = Material.ANVIL;

	public BankGui(Bank bank) {
		super(bank);
	}

	@Override
	Menu getMenu() {
		return ChestMenu.builder(2).title(ChatColor.DARK_GRAY + "Bank Management").build();
	}

	@Override
	void getClearance(Player player) {
		highClearance = guiSubject.isTrusted(player)
				|| (guiSubject.isAdminBank() && player.hasPermission(Permissions.BANK_INFO_ADMIN)
				|| (!guiSubject.isAdminBank() && player.hasPermission(Permissions.BANK_INFO_OTHER)));
	}

	@Override
	ItemStack createSlotItem(int i) {
		switch (i) {
			case 0:
				return createSlotItem(GENERAL_INFO_BLOCK, "General Information", getGeneralInfoLore());
			case 4:
				return createSlotItem(Material.CAKE, "Statistics", getStatisticsLore());
			case 8:
				if (highClearance && !guiSubject.getAccounts().isEmpty())
					return createSlotItem(Material.CHEST, "Accounts", Collections.singletonList("Click here to view accounts."));
				break;

			case 9:
				return createSlotItem(DEFAULT_BLOCK, "Account Creation", getCreationLore());
			case 10:
				return createSlotItem(MULTIPLIER_INFO_BLOCK, "Multipliers", Utils.getMultiplierLore(guiSubject));
			case 11:
				return createSlotItem(Material.IRON_BARS, "Balance Restrictions", getBalanceRestrictionLore());
			case 12:
				return createSlotItem(DEFAULT_BLOCK, "Offline Payouts", getOfflinePayoutsLore());
			case 13:
				return createSlotItem(Material.CLOCK, "Interest Delay", getInterestDelayLore());
			default:
				return new ItemStack(Material.AIR);
		}
		return new ItemStack(Material.AIR);
	}

	@Override
	ClickHandler createClickHandler(int i) {
		switch (i) {
			case 8:
				if (highClearance && !guiSubject.getAccounts().isEmpty())
					return (player, info) -> {
						new AccountListGui(guiSubject).open(player);
					};
			default:
				return (player, info) -> {
				};
		}
	}

	private List<String> getGeneralInfoLore() {
		return Arrays.asList(
				"Owner: " + (guiSubject.isPlayerBank() ? ChatColor.GOLD + guiSubject.getOwnerDisplayName() : ChatColor.RED + "ADMIN"),
				"Co-owners: " + (guiSubject.getCoowners().isEmpty() ? org.bukkit.ChatColor.RED + "[none]"
						: ChatColor.AQUA + guiSubject.getCoowners().stream().map(OfflinePlayer::getName)
						.collect(Collectors.joining(", ", "[ ", " ]"))),
				"Location: " + ChatColor.AQUA + guiSubject.getSelection().getCoordinates()
		);
	}

	private List<String> getStatisticsLore() {
		return Arrays.asList(
				"Number of accounts: " + ChatColor.AQUA + guiSubject.getAccounts().size(),
				"Total value: " + ChatColor.GREEN + "$" + Utils.formatNumber(guiSubject.getTotalValue()),
				"Average account value: " + ChatColor.GREEN + "$" +
						Utils.formatNumber(!guiSubject.getAccounts().isEmpty()
								? guiSubject.getTotalValue().doubleValue() / guiSubject.getAccounts().size()
								: guiSubject.getAccounts().size()),
				"Equality score: " + Utils.getEqualityLore(guiSubject)
		);
	}

	private List<String> getCreationLore() {
		AccountConfig config = guiSubject.getAccountConfig();
		boolean reimburse = config.isReimburseAccountCreation(false);
		return Arrays.asList(
				"Fee: " + ChatColor.GREEN + "$" + Utils.formatNumber(config.getAccountCreationPrice(false)),
				"Fee Reimbursement: " + (reimburse ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No")
		);
	}

	private List<String> getBalanceRestrictionLore() {
		AccountConfig config = guiSubject.getAccountConfig();
		double minBalance = config.getMinBalance(false);
		double lowBalanceFee = config.getLowBalanceFee(false);
		boolean strikethrough = minBalance == 0;
		return Arrays.asList(
				"Minimum balance: " + ChatColor.GREEN + "$" + Utils.formatNumber(minBalance),
				"Low balance fee: " + ChatColor.RED + (strikethrough ? ChatColor.STRIKETHROUGH : "") + "$" + Utils.formatNumber(lowBalanceFee)
		);
	}

	private List<String> getOfflinePayoutsLore() {
		AccountConfig config = guiSubject.getAccountConfig();
		int offlinePayouts = config.getAllowedOfflinePayouts(false);
		int offlineIncrement = config.getOfflineMultiplierBehavior(false);
		int beforeReset = config.getAllowedOfflineBeforeReset(false);
		return Arrays.asList(
				"Accounts will pay interest up to " + ChatColor.AQUA + offlinePayouts + ChatColor.GRAY
						+ " times while the account holder is offline.",
				"Account multipliers will be reset after the account holder has been offline for "
						+ ChatColor.AQUA + beforeReset + ChatColor.GRAY + " interest payouts."
		);
	}

	private List<String> getInterestDelayLore() {
		AccountConfig config = guiSubject.getAccountConfig();
		int interestDelay = config.getInitialInterestDelay(false);
		boolean countOffline = config.isCountInterestDelayOffline(false);
		return Arrays.asList(
				"New accounts will begin to pay interest after " + ChatColor.AQUA + interestDelay + ChatColor.GRAY + " interest cycles.",
				"The account owner " + (countOffline ? "does not have to be" : "must be") + " online for these cycles."
		);
	}
}
