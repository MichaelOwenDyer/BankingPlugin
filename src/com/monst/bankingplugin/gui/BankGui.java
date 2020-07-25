package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
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

	boolean verbose;
	boolean canEdit;
	boolean canListAccounts;

	public BankGui(Bank bank) {
		super(BankingPlugin.getInstance(), bank);
	}

	@Override
	Menu getMenu() {
		return ChestMenu.builder(2).title(guiSubject.getColorizedName()).build();
	}

	@Override
	void evaluateClearance(Player player) {
		verbose = guiSubject.isTrusted(player)
				|| (guiSubject.isAdminBank() && player.hasPermission(Permissions.BANK_INFO_ADMIN)
				|| (!guiSubject.isAdminBank() && player.hasPermission(Permissions.BANK_INFO_OTHER)));
		canEdit = guiSubject.isTrusted(player)
				|| (guiSubject.isAdminBank() && player.hasPermission(Permissions.BANK_SET_ADMIN)
				|| (!guiSubject.isAdminBank() && player.hasPermission(Permissions.BANK_SET_OTHER)));
		canListAccounts = guiSubject.isTrusted(player)
				|| player.hasPermission(Permissions.ACCOUNT_LIST_OTHER);
	}

	@Override
	ItemStack createSlotItem(int i) {
		switch (i) {
			case 0:
				return createSlotItem(GENERAL_INFO_BLOCK, "General Information", getGeneralInfoLore());
			case 4:
				return createSlotItem(Material.CAKE, "Statistics", getStatisticsLore());
			case 8:
				if (canListAccounts && !guiSubject.getAccounts().isEmpty())
					return createSlotItem(Material.CHEST, "Account List", Collections.singletonList("Click here to view accounts."));
				return createSlotItem(Material.CHEST, "Account List", Collections.singletonList(
						canListAccounts ? "There are no accounts at this bank." : "You do not have permission to view this."));
			case 9:
				return createSlotItem(Material.ENCHANTED_BOOK, "Account Creation", getCreationLore());
			case 10:
				return createSlotItem(MULTIPLIER_INFO_BLOCK, "Multipliers", Utils.getMultiplierLore(guiSubject));
			case 11:
				return createSlotItem(Material.IRON_BARS, "Balance Restrictions", getBalanceRestrictionLore());
			case 12:
				return createSlotItem(Material.LIGHT_BLUE_BED, "Offline Payouts", getOfflinePayoutsLore());
			case 13:
				return createSlotItem(Material.CLOCK, "Interest Delay", getInterestDelayLore());
			case 14:
				return createSlotItem(Material.BELL, "Withdrawal Policy", getWithdrawalPolicyLore());
			default:
				return new ItemStack(Material.AIR);
		}
		// return new ItemStack(Material.AIR);
	}

	@Override
	ClickHandler createClickHandler(int i) {
		switch (i) {
			case 8:
				if (canListAccounts && !guiSubject.getAccounts().isEmpty())
					return (player, info) -> {
						new AccountListGui(guiSubject).setPrevGui(this).open(player);
					};
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
		return GuiType.BANK;
	}

	private List<String> getGeneralInfoLore() {
		return Arrays.asList(
				"Owner: " + ChatColor.GOLD + guiSubject.getOwnerDisplayName(),
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
				"Low balance fee: " + ChatColor.RED
						+ (strikethrough ? ChatColor.STRIKETHROUGH : "") + "$" + Utils.formatNumber(lowBalanceFee)
		);
	}

	private List<String> getOfflinePayoutsLore() {
		AccountConfig config = guiSubject.getAccountConfig();
		int offlinePayouts = config.getAllowedOfflinePayouts(false);
		int offlineDecrement = config.getOfflineMultiplierDecrement(false);
		int beforeReset = config.getAllowedOfflineBeforeReset(false);
		return Utils.wordWrapAll(
				"Accounts will pay interest up to " + ChatColor.AQUA + offlinePayouts + ChatColor.GRAY
						+ String.format("time%s", offlinePayouts == 1 ? "" : "s") + " while the account holder is offline.",
				"Account multipliers will be reset after the account holder has been offline for "
						+ ChatColor.AQUA + beforeReset + ChatColor.GRAY + " interest payouts.",
				"Account multipliers will " + (offlineDecrement == 0 ? "remain frozen"
						: "decrease by " + ChatColor.AQUA + offlineDecrement + ChatColor.GRAY
						+ " for every payout event") + " while offline."
		);
	}

	private List<String> getInterestDelayLore() {
		AccountConfig config = guiSubject.getAccountConfig();
		int interestDelay = config.getInitialInterestDelay(false);
		boolean countOffline = config.isCountInterestDelayOffline(false);
		return Utils.wordWrapAll(
				"New accounts will begin to pay interest after "
						+ ChatColor.AQUA + interestDelay + ChatColor.GRAY + " interest cycles.",
				"The account owner " + (countOffline ? "does not have to be" : "must be ")
						+ " online for these cycles."
		);
	}

	private List<String> getWithdrawalPolicyLore() {
		int withdrawalDecrement = guiSubject.getAccountConfig().getWithdrawalMultiplierDecrement(false);
		return Utils.wordWrapAll(
				"Account multipliers will " + (withdrawalDecrement == 0 ? "not be affected" : "decrease by "
						+ ChatColor.AQUA + withdrawalDecrement + ChatColor.GRAY
						+ String.format("stage%s", withdrawalDecrement == 1 ? "" : "s")) + " on withdrawal."
		);
	}
}
