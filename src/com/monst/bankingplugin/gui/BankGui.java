package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.utils.AccountConfig;
import com.monst.bankingplugin.utils.BankUtils;
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

	boolean verbose;
	boolean canEdit;
	boolean canListAccounts;

	public BankGui(Bank bank) {
		super(BankingPlugin.getInstance(), bank);
	}

	@Override
	void createMenu() {
		menu = ChestMenu.builder(2).title(guiSubject.getColorizedName()).build();
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
				if (guiSubject.isPlayerBank())
					return createSlotItem(guiSubject.getOwner(), "General Information", getGeneralInfoLore());
				return createSlotItem(Material.PLAYER_HEAD, "General Information", getGeneralInfoLore());
			case 4:
				return createSlotItem(Material.CAKE, "Statistics", getStatisticsLore());
			case 8:
				if (canListAccounts)
					return createSlotItem(Material.CHEST, "Account List", Collections.singletonList(guiSubject
							.getAccounts().isEmpty() ? "There are no accounts at this bank." : "Click here to view accounts."));
				return createSlotItem(Material.CHEST, "Account List", NO_PERMISSION);
			case 9:
				return createSlotItem(Material.ENCHANTED_BOOK, "Account Creation", getCreationLore());
			case 10:
				return createSlotItem(Material.NETHER_STAR, "Multipliers", Utils.getMultiplierLore(guiSubject));
			case 11:
				return createSlotItem(Material.IRON_BARS, "Balance Restrictions", getBalanceRestrictionLore());
			case 12:
				return createSlotItem(Material.LIGHT_BLUE_BED, "Offline Payouts", getOfflinePayoutsLore());
			case 13:
				return createSlotItem(Material.CLOCK, "Interest Delay", getInterestDelayLore());
			case 14:
				return createSlotItem(Material.BELL, "Withdrawal Policy", getWithdrawalPolicyLore());
			case 15:
				return createSlotItem(Material.TOTEM_OF_UNDYING, "Account Limit", getAccountLimitLore());
			default:
				return new ItemStack(Material.AIR);
		}
	}

	@Override
	@SuppressWarnings("all")
	ClickHandler createClickHandler(int i) {
		switch (i) {
			case 8:
				if (canListAccounts && !guiSubject.getAccounts().isEmpty())
					return (player, info) -> new AccountListGui(guiSubject).setPrevGui(this).open(player);
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
				"Number of unique customers: " + ChatColor.AQUA + guiSubject.getCustomerAccounts().keySet().size(),
				"Total value: " + ChatColor.GREEN + "$" + Utils.format(guiSubject.getTotalValue())
						+ ChatColor.GRAY + " (rank " + plugin.getBankUtils().getTotalValueRanking(guiSubject)
						+ " of " + plugin.getBankUtils().getBanks().size() + ")",
				"Average account value: " + ChatColor.GREEN + "$" +
						Utils.format(!guiSubject.getAccounts().isEmpty()
								? guiSubject.getTotalValue().doubleValue() / guiSubject.getAccounts().size()
								: guiSubject.getAccounts().size()),
				"Equality score: " + BankUtils.getEqualityLore(guiSubject)
		);
	}

	private List<String> getCreationLore() {
		AccountConfig config = guiSubject.getAccountConfig();
		boolean reimburse = config.get(AccountConfig.Field.REIMBURSE_ACCOUNT_CREATION);
		return Arrays.asList(
				ChatColor.GRAY + "Fee: " + ChatColor.GREEN + "$" + Utils.format(config.get(AccountConfig.Field.ACCOUNT_CREATION_PRICE)),
				ChatColor.GRAY + "Reimbursement on removal: " + (reimburse ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No")
		);
	}

	private List<String> getBalanceRestrictionLore() {
		AccountConfig config = guiSubject.getAccountConfig();
		double minBalance = config.get(AccountConfig.Field.MINIMUM_BALANCE);
		double lowBalanceFee = config.get(AccountConfig.Field.LOW_BALANCE_FEE);
		boolean strikethrough = minBalance == 0;
		boolean payOnLowBalance = config.get(AccountConfig.Field.PAY_ON_LOW_BALANCE);
		return Utils.wordWrapAll(
				ChatColor.GRAY + "Minimum balance: " + ChatColor.GREEN + "$" + Utils.format(minBalance),
				ChatColor.GRAY + "Low balance fee: " + ChatColor.RED
						+ (strikethrough ? ChatColor.STRIKETHROUGH : "") + "$" + Utils.format(lowBalanceFee),
				"",
				ChatColor.GRAY + "Interest " + (payOnLowBalance ? ChatColor.GREEN + "will" : ChatColor.RED + "will not")
						+ " continue to be " + ChatColor.GRAY + "paid out when the account balance is low."
		);
	}

	private List<String> getOfflinePayoutsLore() {
		AccountConfig config = guiSubject.getAccountConfig();
		int offlinePayouts = config.get(AccountConfig.Field.ALLOWED_OFFLINE_PAYOUTS);
		int offlineDecrement = config.get(AccountConfig.Field.OFFLINE_MULTIPLIER_DECREMENT);
		int beforeReset = config.get(AccountConfig.Field.ALLOWED_OFFLINE_PAYOUTS_BEFORE_RESET);
		return Utils.wordWrapAll(
				ChatColor.GRAY + "Accounts may generate interest up to " + ChatColor.AQUA + offlinePayouts + ChatColor.GRAY
						+ String.format(" time%s", offlinePayouts == 1 ? "" : "s") + " while account holders are offline.",
				"",
				ChatColor.GRAY + "Multipliers will " + (offlineDecrement == 0
						? "freeze"
						: "decrease by " + ChatColor.AQUA + offlineDecrement + ChatColor.GRAY + " for every payout")
						+ " during this time.",
				"",
				ChatColor.GRAY + "Account multipliers will reset " + (beforeReset == 0
						? "immediately"
						: "after generating interest " + ChatColor.AQUA + beforeReset + ChatColor.GRAY + " consecutive "
							+ String.format("time%s", beforeReset == 1 ? "" : "s")) + " while account holders are offline."

		);
	}

	private List<String> getInterestDelayLore() {
		AccountConfig config = guiSubject.getAccountConfig();
		int interestDelay = config.get(AccountConfig.Field.INITIAL_INTEREST_DELAY);
		boolean countOffline = config.get(AccountConfig.Field.COUNT_INTEREST_DELAY_OFFLINE);
		return Utils.wordWrapAll(
				ChatColor.GRAY + "New accounts will begin to generate interest after "
						+ ChatColor.AQUA + interestDelay + ChatColor.GRAY + " preliminary interest cycles.",
				"",
				ChatColor.GRAY + "The account owner " + (countOffline
						? ChatColor.GREEN + "does not have to be online"
						: ChatColor.RED + "must be online")
						+ ChatColor.GRAY + " for these cycles to be counted toward the delay."
		);
	}

	private List<String> getWithdrawalPolicyLore() {
		int withdrawalDecrement = guiSubject.getAccountConfig().get(AccountConfig.Field.WITHDRAWAL_MULTIPLIER_DECREMENT);
		return Utils.wordWrapAll(
				ChatColor.GRAY + "Account multipliers will " + (withdrawalDecrement == 0
						? ChatColor.GREEN + "not be affected" + ChatColor.GRAY
						: "decrease by " + ChatColor.AQUA + withdrawalDecrement + ChatColor.GRAY
						+ String.format(" stage%s", withdrawalDecrement == 1 ? "" : "s")) + " on withdrawal."
		);
	}

	private List<String> getAccountLimitLore() {
		int accountLimit = guiSubject.getAccountConfig().get(AccountConfig.Field.PLAYER_BANK_ACCOUNT_LIMIT);
		return Utils.wordWrapAll(
				ChatColor.GRAY + (accountLimit == 0
						? "Account creation is currently " + ChatColor.RED + "disabled" + ChatColor.GRAY
						: "Players may create " + (accountLimit > 0
								? "up to " + ChatColor.AQUA + accountLimit
								: ChatColor.GREEN + "unlimited") + ChatColor.GRAY + " accounts at this bank.")
		);
	}
}
