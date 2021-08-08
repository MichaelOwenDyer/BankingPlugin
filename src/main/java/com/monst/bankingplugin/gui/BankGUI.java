package com.monst.bankingplugin.gui;

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
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class BankGUI extends SinglePageGUI<Bank> {

	boolean canTP;
	boolean canListAccounts;

	public BankGUI(Bank bank) {
		super(bank);
	}

	@Override
	Menu createMenu() {
		return ChestMenu.builder(2).title(guiSubject.getColorizedName()).build();
	}

	@Override
	void evaluateClearance(Player player) {
		canTP = player.isOp()
				|| Permissions.hasAny(player, "minecraft.command.tp", "essentials.tp.position");
		canListAccounts = guiSubject.isTrusted(player)
				|| player.hasPermission(Permissions.ACCOUNT_LIST_OTHER);
	}

	@Override
	ItemStack createSlotItem(int slot) {
		switch (slot) {
			case 0:
				if (guiSubject.isPlayerBank())
					return createSlotItem(guiSubject.getOwner(), "General Information", getGeneralInfoLore());
				return createSlotItem(Material.PLAYER_HEAD, "General Information", getGeneralInfoLore());
			case 4:
				return createSlotItem(Material.CAKE, "Statistics", getStatisticsLore());
			case 7:
				if (canListAccounts)
					return createSlotItem(Material.BOOKSHELF, "Income Log", Collections.emptyList());
			case 8:
				if (canListAccounts)
					return createSlotItem(Material.CHEST, "Account List",
							Collections.singletonList(guiSubject.hasAccounts() ?
									"Click here to view accounts." : "There are no accounts at this bank."));
				return createSlotItem(Material.CHEST, "Account List", NO_PERMISSION);
			case 9:
				return createSlotItem(Material.ENCHANTED_BOOK, "Account Creation", getCreationLore());
			case 10:
				return createSlotItem(Material.NETHER_STAR, "Multipliers", getMultiplierLore());
			case 11:
				return createSlotItem(Material.IRON_BARS, "Balance Restrictions", getBalanceRestrictionLore());
			case 12:
				return createSlotItem(Material.LIGHT_BLUE_BED, "Offline Payouts", getOfflinePayoutsLore());
			case 13:
				return createSlotItem(Material.GOLD_INGOT, "Interest Rate", getInterestRateLore());
			case 14:
				return createSlotItem(Material.COMPASS, "Interest Delay", getInterestDelayLore());
			case 15:
				return createSlotItem(Material.BELL, "Withdrawal Policy", getWithdrawalPolicyLore());
			case 16:
				return createSlotItem(Material.TOTEM_OF_UNDYING, "Account Limit", getAccountLimitLore());
			case 17:
				return createSlotItem(Material.CLOCK, "Interest Payout Times", getPayoutTimeLore());
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
						if (info.getClickType().isLeftClick())
							Utils.teleport(player, guiSubject.getRegion().getTeleportLocation());
						else if (info.getClickType().isRightClick())
							Utils.teleport(player, guiSubject.getRegion().getHighestTeleportLocation());
						exit(player);
					};
			case 7:
				if (canListAccounts)
					return (player, info) -> new BankIncomeGUI(guiSubject).setParentGUI(this).open(player);
			case 8:
				if (canListAccounts && guiSubject.hasAccounts())
					return (player, info) -> new AccountListGUI(guiSubject::getAccounts).setParentGUI(this).open(player);
		}
		return null;
	}

	@Override
	GUIType getType() {
		return GUIType.BANK;
	}

	private List<String> getGeneralInfoLore() {
		Stream.Builder<String> lore = Stream.builder();
		lore.add("Bank ID: " + guiSubject.getID());
		lore.add("Owner: " + ChatColor.GOLD + guiSubject.getOwnerDisplayName());
		lore.add("Co-owners: " + (guiSubject.getCoOwners().isEmpty()
				? ChatColor.RED + "[none]"
				: ChatColor.AQUA + Utils.map(guiSubject.getCoOwners(), OfflinePlayer::getName).toString())
		);
		lore.add("Location: " + ChatColor.AQUA + guiSubject.getRegion().getCoordinates());
		if (canTP)
			lore.add("Click to teleport to bank.");
		return wordWrapAll(45, lore.build());
	}

	private List<String> getStatisticsLore() {
		return Arrays.asList(
				"Total account value: " + Utils.formatAndColorize(guiSubject.getTotalValue()),
				"Average account value: " + Utils.formatAndColorize(guiSubject.getAverageValue()),
				"Number of accounts: " + ChatColor.AQUA + guiSubject.getAccounts().size(),
				"Number of account holders: " + ChatColor.AQUA + guiSubject.getAccountHolders().size(),
				"Equality score: " + getEqualityLore(guiSubject)
		);
	}

	static String getEqualityLore(Bank bank) {
		double gini = 1 - bank.getGiniCoefficient();
		ChatColor color;
		String assessment = "";
		switch ((int) (gini * 5)) {
			case 0:
				color = ChatColor.DARK_RED;
				assessment = "(Very Poor)";
				break;
			case 1:
				color = ChatColor.RED;
				assessment = "(Poor)";
				break;
			case 2:
				color = ChatColor.YELLOW;
				assessment = "(Good)";
				break;
			case 3:
				color = ChatColor.GREEN;
				assessment = "(Very Good)";
				break;
			case 4: case 5:
				color = ChatColor.DARK_GREEN;
				assessment = "(Excellent)";
				break;
			default:
				color = ChatColor.GRAY;
		}
		return "" + color + Math.round(gini * 100) + "% " + assessment;
	}

	private List<String> getCreationLore() {
		boolean reimburse = guiSubject.getReimburseAccountCreation().get();
		return Arrays.asList(
				"Fee per chest: " + ChatColor.GREEN + guiSubject.getAccountCreationPrice().getFormatted(),
				"Reimbursed on removal: " + (reimburse ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No")
		);
	}

	private List<String> getMultiplierLore() {
		return getMultiplierLore(guiSubject.getMultipliers().get(), -1);
	}

	private List<String> getBalanceRestrictionLore() {
		BigDecimal minBalance = guiSubject.getMinimumBalance().get();
		BigDecimal lowBalanceFee = guiSubject.getLowBalanceFee().get();
		boolean strikethrough = minBalance.signum() == 0;
		boolean payOnLowBalance = guiSubject.getPayOnLowBalance().get();
		Stream.Builder<String> lore = Stream.builder();
		lore.add("Minimum balance: " + ChatColor.GREEN + Utils.format(minBalance));
		lore.add("Low balance fee: " + ChatColor.RED + (strikethrough ? ChatColor.STRIKETHROUGH : "") + Utils.format(lowBalanceFee));
		if (!strikethrough) {
			lore.add("");
			lore.add("Interest " + (payOnLowBalance ? ChatColor.GREEN + "will" : ChatColor.RED + "will not")
					+ " continue " + ChatColor.GRAY + "to be paid out when the account balance is low.");
		}
		return wordWrapAll(38, lore.build());
	}

	private List<String> getOfflinePayoutsLore() {
		int offlinePayouts = guiSubject.getAllowedOfflinePayouts().get();
		int offlineDecrement = guiSubject.getOfflineMultiplierDecrement().get();
		Stream.Builder<String> lore = Stream.builder();
		lore.add("Accounts will " + (offlinePayouts == 0 ? ChatColor.RED + "not generate interest" + ChatColor.GRAY
				: "generate interest up to " + ChatColor.AQUA + offlinePayouts + ChatColor.GRAY
				+ String.format(" time%s", offlinePayouts == 1 ? "" : "s")) + " while account holders are offline."
		);
		lore.add("");
		lore.add("Account multipliers will " + (offlineDecrement == 0
				? ChatColor.AQUA + "freeze" + ChatColor.GRAY
				: "decrease by " + ChatColor.AQUA + offlineDecrement + ChatColor.GRAY + " for every payout")
			+ " while account holders are offline.");
		return wordWrapAll(lore.build());
	}

	private List<String> getInterestRateLore() {
		return wordWrapAll(
			ChatColor.GREEN + guiSubject.getInterestRate().getFormatted()
		);
	}

	private List<String> getInterestDelayLore() {
		int interestDelay = guiSubject.getInitialInterestDelay().get();
		boolean countOffline = guiSubject.getCountInterestDelayOffline().get();
		Stream.Builder<String> lore = Stream.builder();
		lore.add("New accounts will begin to generate interest " + (interestDelay == 0
				? ChatColor.GREEN + "immediately" + ChatColor.GRAY + " after creation."
				: "after " + ChatColor.AQUA + interestDelay + ChatColor.GRAY +
					String.format(" preliminary interest cycle%s.", interestDelay == 1 ? "" : "s")));
		if (interestDelay != 0) {
			lore.add("");
			lore.add("The account owner " + (countOffline
					? ChatColor.GREEN + "does not have to be online"
					: ChatColor.RED + "must be online")
						+ ChatColor.GRAY + " for these cycles to be counted toward the delay.");
		}
		return wordWrapAll(lore.build());
	}

	private List<String> getWithdrawalPolicyLore() {
		int withdrawalDecrement = guiSubject.getWithdrawalMultiplierDecrement().get();
		return wordWrapAll(
				"Account multipliers will " + (withdrawalDecrement == 0
						? ChatColor.GREEN + "not be affected on" + ChatColor.GRAY
						: "decrease by " + ChatColor.AQUA + withdrawalDecrement + ChatColor.GRAY
							+ String.format(" stage%s upon each", withdrawalDecrement == 1 ? "" : "s")) + " withdrawal."
		);
	}

	private List<String> getAccountLimitLore() {
		int accountLimit = guiSubject.getPlayerBankAccountLimit().get();
		return wordWrapAll(
				(accountLimit == 0
						? "Account creation is currently " + ChatColor.RED + "disabled" + ChatColor.GRAY
						: "Players may create " + (accountLimit > 0
								? "up to " + ChatColor.AQUA + accountLimit
								: ChatColor.GREEN + "unlimited") + ChatColor.GRAY
						+ String.format(" account%s at this bank.", accountLimit == 1 ? "" : "s"))
		);
	}

	private List<String> getPayoutTimeLore() {
		Set<LocalTime> times = guiSubject.getInterestPayoutTimes().get();
		Stream.Builder<String> lore = Stream.builder();
		if (!times.isEmpty()) {
			lore.add("Accounts will generate interest every day at: ");
			for (LocalTime time : times)
				lore.add(ChatColor.GOLD + " - " + time.toString());
		} else
			lore.add("Accounts will not generate interest.");
		return wordWrapAll(lore.build());
	}
}
