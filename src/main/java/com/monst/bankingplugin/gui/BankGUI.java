package com.monst.bankingplugin.gui;

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
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BankGUI extends SinglePageGUI<Bank> {

	boolean canTP;
	boolean canListAccounts;

	public BankGUI(Bank bank) {
		super(bank);
	}

	@Override
	void initializeMenu() {
		menu = ChestMenu.builder(2).title(guiSubject.getColorizedName()).build();
	}

	@Override
	void evaluateClearance(Player player) {
		canTP = player.isOp()
				|| player.hasPermission("minecraft.command.tp")
				|| player.hasPermission("essentials.tp.position");
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
					return createSlotItem(Material.CHEST, "Account List",
							Collections.singletonList(guiSubject.getAccounts().isEmpty() ?
									"There are no accounts at this bank." : "Click here to view accounts."));
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
	ClickHandler createClickHandler(int i) {
		switch (i) {
			case 0:
				return canTP ? (player, info) -> {
					if (info.getClickType().isLeftClick())
						player.teleport(Utils.getSafeLocation(guiSubject.getSelection().getCenterPoint().toLocation(guiSubject.getSelection().getWorld()))
								.setDirection(player.getLocation().getDirection()));
					else if (info.getClickType().isRightClick())
						player.teleport(guiSubject.getSelection()
								.getWorld()
								.getHighestBlockAt(guiSubject.getSelection().getCenterPoint().toLocation(guiSubject.getSelection().getWorld()))
								.getLocation().add(0.5, 1, 0.5)
								.setDirection(player.getLocation().getDirection()));
					this.close(player);
				} : null;
			case 8:
				if (canListAccounts && !guiSubject.getAccounts().isEmpty())
					return (player, info) -> new AccountListGUI(guiSubject::getAccounts).setParentGUI(this).open(player);
				else
					return null;
			default:
				return null;
		}
	}

	@Override
	void setCloseHandler(Menu.CloseHandler handler) {
		menu.setCloseHandler(handler);
	}

	@Override
	GUIType getType() {
		return GUIType.BANK;
	}

	private List<String> getGeneralInfoLore() {
		List<String> lore = new ArrayList<>();
		lore.add("Bank ID: " + guiSubject.getID());
		lore.add("Owner: " + ChatColor.GOLD + guiSubject.getOwnerDisplayName());
		lore.add("Co-owners: " + (guiSubject.getCoowners().isEmpty()
				? ChatColor.RED + "[none]"
				: ChatColor.AQUA + Utils.map(guiSubject.getCoowners(), OfflinePlayer::getName).toString())
		);
		lore.add("Location: " + ChatColor.AQUA + guiSubject.getSelection().getCoordinates());
		if (canTP)
			lore.add("Click to teleport to bank.");
		return wordWrapAll(45, lore);
	}

	private List<String> getStatisticsLore() {
		return Arrays.asList(
				"Number of accounts: " + ChatColor.AQUA + guiSubject.getAccounts().size(),
				"Number of account holders: " + ChatColor.AQUA + guiSubject.getAccountsByOwner().keySet().size(),
				"Total value: " + ChatColor.GREEN + Utils.format(guiSubject.getTotalValue()),
				"Average account value: " + ChatColor.GREEN +
						Utils.format(guiSubject.getAccounts().isEmpty()
								? BigDecimal.ZERO
								: guiSubject.getTotalValue().divide(BigDecimal.valueOf(guiSubject.getAccounts().size()), RoundingMode.HALF_EVEN)),
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
		boolean reimburse = guiSubject.get(BankField.REIMBURSE_ACCOUNT_CREATION);
		return Arrays.asList(
				"Fee per chest: " + ChatColor.GREEN + guiSubject.getFormatted(BankField.ACCOUNT_CREATION_PRICE),
				"Reimbursed on removal: " + (reimburse ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No")
		);
	}

	private List<String> getMultiplierLore() {
		return getMultiplierLore(guiSubject.get(BankField.MULTIPLIERS), -1);
	}

	private List<String> getBalanceRestrictionLore() {
		double minBalance = guiSubject.get(BankField.MINIMUM_BALANCE);
		double lowBalanceFee = guiSubject.get(BankField.LOW_BALANCE_FEE);
		boolean strikethrough = minBalance == 0;
		boolean payOnLowBalance = guiSubject.get(BankField.PAY_ON_LOW_BALANCE);
		List<String> lore = new ArrayList<>();
		lore.add("Minimum balance: " + ChatColor.GREEN + Utils.format(minBalance));
		lore.add("Low balance fee: " + ChatColor.RED + (strikethrough ? ChatColor.STRIKETHROUGH : "") + Utils.format(lowBalanceFee));
		if (!strikethrough) {
			lore.add("");
			lore.add("Interest " + (payOnLowBalance ? ChatColor.GREEN + "will" : ChatColor.RED + "will not")
					+ " continue " + ChatColor.GRAY + "to be paid out when the account balance is low.");
		}
		return wordWrapAll(38, lore);
	}

	private List<String> getOfflinePayoutsLore() {
		int offlinePayouts = guiSubject.get(BankField.ALLOWED_OFFLINE_PAYOUTS);
		int offlineDecrement = guiSubject.get(BankField.OFFLINE_MULTIPLIER_DECREMENT);
		int beforeReset = guiSubject.get(BankField.ALLOWED_OFFLINE_PAYOUTS_BEFORE_RESET);
		List<String> lore = new ArrayList<>();
		lore.add("While account holders are offline...");
		lore.add("");
		lore.add("accounts will " + (offlinePayouts == 0 ? ChatColor.RED + "not generate interest" + ChatColor.GRAY
				: "generate interest up to " + ChatColor.AQUA + offlinePayouts + ChatColor.GRAY
				+ String.format(" time%s", offlinePayouts == 1 ? "" : "s")) + "."
		);
		if (beforeReset != 0) {
			lore.add("");
			lore.add("account multipliers will " + (offlineDecrement == 0
					? ChatColor.AQUA + "freeze" + ChatColor.GRAY
					: "decrease by " + ChatColor.AQUA + offlineDecrement + ChatColor.GRAY + " for every payout")
				+ ".");
		}
		if (beforeReset > -1) {
			lore.add("");
			lore.add("Multipliers will reset " + (beforeReset == 0 ? ChatColor.RED + "before paying out interest" + ChatColor.GRAY
					: "after generating interest " + ChatColor.AQUA + beforeReset + ChatColor.GRAY
					+ String.format(" time%s", beforeReset == 1 ? "" : "s")) + ".");
		}
		return wordWrapAll(lore);
	}

	private List<String> getInterestRateLore() {
		double interestRate = guiSubject.get(BankField.INTEREST_RATE);
		return wordWrapAll(
			"" + ChatColor.GREEN + String.format("%,.1f", interestRate * 100) + "%"
		);
	}

	private List<String> getInterestDelayLore() {
		int interestDelay = guiSubject.get(BankField.INITIAL_INTEREST_DELAY);
		boolean countOffline = guiSubject.get(BankField.COUNT_INTEREST_DELAY_OFFLINE);
		List<String> lore = new ArrayList<>();
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
		return wordWrapAll(lore);
	}

	private List<String> getWithdrawalPolicyLore() {
		int withdrawalDecrement = guiSubject.get(BankField.WITHDRAWAL_MULTIPLIER_DECREMENT);
		return wordWrapAll(
				"Account multipliers will " + (withdrawalDecrement == 0
						? ChatColor.GREEN + "not be affected on" + ChatColor.GRAY
						: "decrease by " + ChatColor.AQUA + withdrawalDecrement + ChatColor.GRAY
							+ String.format(" stage%s upon each", withdrawalDecrement == 1 ? "" : "s")) + " withdrawal."
		);
	}

	private List<String> getAccountLimitLore() {
		int accountLimit = guiSubject.get(BankField.PLAYER_BANK_ACCOUNT_LIMIT);
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
		List<LocalTime> times = guiSubject.get(BankField.INTEREST_PAYOUT_TIMES);
		List<String> lore = new ArrayList<>();
		if (!times.isEmpty()) {
			lore.add("Accounts will generate interest every day at: ");
			for (LocalTime time : times)
				lore.add(ChatColor.GOLD + " - " + time.toString());
		} else
			lore.add("Accounts will not generate interest.");
		return wordWrapAll(lore);
	}
}
