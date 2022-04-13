package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.lang.ColorStringBuilder;
import com.monst.bankingplugin.util.Permission;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.bukkit.ChatColor.*;

public class BankGUI extends SinglePageGUI<Bank> {

	boolean canTP;
	boolean canListAccounts;

	public BankGUI(BankingPlugin plugin, Bank bank) {
		super(plugin, bank);
	}

	@Override
	Menu createMenu() {
		return ChestMenu.builder(2).title(guiSubject.getColorizedName()).build();
	}

	@Override
	void evaluateClearance(Player player) {
		canTP = player.isOp()
				|| player.hasPermission("minecraft.command.tp")
				|| player.hasPermission("essentials.tp.position");
		canListAccounts = guiSubject.isTrusted(player) || Permission.ACCOUNT_LIST_OTHER.ownedBy(player);
	}

	@Override
	ItemStack createSlotItem(int slot) {
		switch (slot) {
			case 0:
				if (guiSubject.isPlayerBank())
					return createSlotItem(guiSubject.getOwner(), "General Information", getGeneralInfoLore());
				return createSlotItem(Material.PLAYER_HEAD, "General Information", getGeneralInfoLore());
			case 7:
				if (canListAccounts)
					return createSlotItem(Material.BOOKSHELF, "Income Log", Collections.emptyList());
			case 8:
				if (canListAccounts)
					return createSlotItem(Material.CHEST, "Account List",
							Collections.singletonList(guiSubject.getNumberOfAccounts() > 0 ?
									"Click here to view accounts." : "There are no accounts at this bank."));
				return createSlotItem(Material.CHEST, "Account List", NO_PERMISSION);
			case 9:
				return createSlotItem(Material.ENCHANTED_BOOK, "Account Creation", getCreationLore());
			case 10:
				return createSlotItem(Material.NETHER_STAR, "Account Interest Multipliers", getMultipliersLore());
			case 11:
				return createSlotItem(Material.IRON_BARS, "Minimum Account Balance", getMinimumBalanceLore());
			case 12:
				return createSlotItem(Material.RED_BED, "Offline Payouts", getOfflinePayoutsLore());
			case 13:
				return createSlotItem(Material.DIAMOND, "Interest Rate", getInterestRateLore());
			case 14:
				return createSlotItem(Material.HOPPER, "Withdrawal Policy", getWithdrawalPolicyLore());
			case 15:
				return createSlotItem(Material.TOTEM_OF_UNDYING, "Account Limit", getAccountLimitLore());
			case 16:
				return createSlotItem(Material.CLOCK, "Interest Payout Times", getPayoutTimeLore());
			case 17:
				return createSlotItem(Material.CAKE, "Statistics", getStatisticsLore());
			default:
				return null;
		}
	}

	@Override
	ClickHandler createClickHandler(int slot) {
		switch (slot) {
			case 0:
				if (canTP)
					return (player, info) -> {
						if (info.getClickType().isLeftClick())
							teleport(player, guiSubject.getRegion().getTeleportLocation());
						else if (info.getClickType().isRightClick())
							teleport(player, guiSubject.getRegion().getHighestTeleportLocation());
						exit(player);
					};
			case 7:
				if (canListAccounts)
					return (player, info) -> new BankIncomeGUI(plugin, guiSubject).setParentGUI(this).open(player);
			case 8:
				if (canListAccounts && guiSubject.getNumberOfAccounts() > 0)
					return (player, info) -> new AccountListGUI(plugin, guiSubject).setParentGUI(this).open(player);
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
		if (guiSubject.isPlayerBank())
			lore.add("Owner: " + GOLD + guiSubject.getOwner().getName());
		if (guiSubject.hasCoOwners())
			lore.add("Co-owners: " + AQUA + guiSubject.getCoOwners().stream()
					.map(OfflinePlayer::getName)
					.collect(Collectors.joining(", ")));
		lore.add("Location: " + AQUA + guiSubject.getRegion().toString());
		if (canTP)
			lore.add("Click to teleport to bank.");
		return wordWrapAll(45, lore.build());
	}

	private List<String> getStatisticsLore() {
		return Arrays.asList(
				"Total account value: " + formatAndColorize(guiSubject.getTotalValue()),
				"Average account value: " + formatAndColorize(guiSubject.getAverageValue()),
				"Number of accounts: " + AQUA + guiSubject.getNumberOfAccounts(),
				"Number of account holders: " + AQUA + guiSubject.getAccountHolders().size(),
				"Equality score: " + getEqualityLore(guiSubject)
		);
	}

	static String getEqualityLore(Bank bank) {
		ColorStringBuilder builder = new ColorStringBuilder();
		int gini = BigDecimal.ONE.subtract(bank.getGiniCoefficient()).scaleByPowerOfTen(2).intValue();
		if (gini < 20)
			builder.darkRed(gini).append("%").append(" (Very poor)");
		else if (gini < 40)
			builder.red(gini).append("%").append(" (Poor)");
		else if (gini < 60)
			builder.yellow(gini).append("%").append(" (Good)");
		else if (gini < 80)
			builder.green(gini).append("%").append(" (Very good)");
		else
			builder.darkGreen(gini).append("%").append(" (Excellent)");
		return builder.toString();
	}

	private List<String> getCreationLore() {
		boolean reimburse = plugin.config().reimburseAccountCreation.at(guiSubject);
		return Arrays.asList(
				"Fee per chest: " + GREEN + plugin.config().accountCreationPrice.at(guiSubject),
				"Reimbursed on removal: " + (reimburse ? GREEN + "Yes" : RED + "No")
		);
	}

	private List<String> getMultipliersLore() {
		return getInterestMultiplierLore(plugin.config().interestMultipliers.at(guiSubject), -1);
	}

	private List<String> getMinimumBalanceLore() {
		BigDecimal minBalance = plugin.config().minimumBalance.at(guiSubject);
		BigDecimal lowBalanceFee = plugin.config().lowBalanceFee.at(guiSubject);
		boolean strikethrough = minBalance.signum() == 0;
		boolean payOnLowBalance = plugin.config().payOnLowBalance.at(guiSubject);
		Stream.Builder<String> lore = Stream.builder();
		lore.add("Minimum balance: " + GREEN + format(minBalance));
		lore.add("Low balance fee: " + RED + (strikethrough ? STRIKETHROUGH : "") + format(lowBalanceFee));
		if (!strikethrough) {
			lore.add("");
			lore.add("Interest " + (payOnLowBalance ? GREEN + "will" : RED + "will not")
					+ " continue " + GRAY + "to be paid out when the account balance is low.");
		}
		return wordWrapAll(38, lore.build());
	}

	private List<String> getOfflinePayoutsLore() {
		int offlinePayouts = plugin.config().allowedOfflinePayouts.at(guiSubject);
		int offlineDecrement = plugin.config().offlineMultiplierDecrement.at(guiSubject);
		Stream.Builder<String> lore = Stream.builder();
		lore.add("Accounts will " + (offlinePayouts == 0 ? RED + "not generate interest" + GRAY
				: "generate interest up to " + AQUA + offlinePayouts + GRAY
				+ String.format(" time%s", offlinePayouts == 1 ? "" : "s")) + " while account holders are offline."
		);
		lore.add("");
		lore.add("Account multipliers will " + (offlineDecrement == 0
				? AQUA + "freeze" + GRAY
				: "decrease by " + AQUA + offlineDecrement + GRAY + " for every payout")
			+ " while account holders are offline.");
		return wordWrapAll(lore.build());
	}

	private List<String> getInterestRateLore() {
		BigDecimal ir = plugin.config().interestRate.at(guiSubject);
		return wordWrapAll(
			GREEN + plugin.config().interestRate.format(ir)
		);
	}

	private List<String> getWithdrawalPolicyLore() {
		int withdrawalDecrement = plugin.config().withdrawalMultiplierDecrement.at(guiSubject);
		return wordWrapAll(
				"Account multipliers will " + (withdrawalDecrement == 0
						? GREEN + "not be affected on" + GRAY
						: "decrease by " + AQUA + withdrawalDecrement + GRAY
							+ String.format(" stage%s upon each", withdrawalDecrement == 1 ? "" : "s")) + " withdrawal."
		);
	}

	private List<String> getAccountLimitLore() {
		int accountLimit = plugin.config().playerBankAccountLimit.at(guiSubject);
		return wordWrapAll(
				(accountLimit == 0
						? "Account creation is currently " + RED + "disabled" + GRAY
						: "Players may create " + (accountLimit > 0
								? "up to " + AQUA + accountLimit
								: GREEN + "unlimited") + GRAY
						+ String.format(" account%s at this bank.", accountLimit == 1 ? "" : "s"))
		);
	}

	private List<String> getPayoutTimeLore() {
		Set<LocalTime> times = plugin.config().interestPayoutTimes.at(guiSubject);
		Stream.Builder<String> lore = Stream.builder();
		if (!times.isEmpty()) {
			lore.add("Accounts will generate interest every day at: ");
			for (LocalTime time : times)
				lore.add(GOLD + " - " + time.toString());
		} else
			lore.add("Accounts will not generate interest.");
		return wordWrapAll(lore.build());
	}
}
