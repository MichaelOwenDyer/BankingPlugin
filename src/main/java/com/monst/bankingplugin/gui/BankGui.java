package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.utils.AccountConfig;
import com.monst.bankingplugin.utils.BankUtils;
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

public class BankGui extends SinglePageGui<Bank> {

	boolean canTP;
	boolean canListAccounts;

	public BankGui(Bank bank) {
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
							Collections.singletonList(guiSubject.getAccounts().isEmpty()
									? "There are no accounts at this bank." : "Click here to view accounts."));
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
						player.teleport(Utils.getSafeLocation(guiSubject.getSelection().getCenterPoint())
								.setDirection(player.getLocation().getDirection()));
					else
						player.teleport(guiSubject.getSelection()
								.getWorld()
								.getHighestBlockAt(guiSubject.getSelection().getCenterPoint())
								.getLocation().add(0, 1, 0)
								.setDirection(player.getLocation().getDirection()));
					this.close(player);
				} : null;
			case 8:
				return canListAccounts && !guiSubject.getAccounts().isEmpty()
						? (player, info) -> new AccountListGui(guiSubject.getAccounts()).setPrevGui(this).open(player)
						: null;
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
		List<String> lore = new ArrayList<>();
		lore.add("ID: " + guiSubject.getID());
		lore.add("Owner: " + ChatColor.GOLD + guiSubject.getOwnerDisplayName());
		lore.add("Co-owners: " + (guiSubject.getCoowners().isEmpty()
				? org.bukkit.ChatColor.RED + "[none]"
				: ChatColor.AQUA + Utils.map(guiSubject.getCoowners(), OfflinePlayer::getName).toString())
		);
		lore.add("Location: " + ChatColor.AQUA + guiSubject.getSelection().getCoordinates());
		if (canTP)
			lore.add("Click to teleport to bank.");
		return Utils.wordWrapAll(55, lore);
	}

	private List<String> getStatisticsLore() {
		return Arrays.asList(
				"Number of accounts: " + ChatColor.AQUA + guiSubject.getAccounts().size(),
				"Number of unique customers: " + ChatColor.AQUA + guiSubject.getAccountsByOwner().keySet().size(),
				"Total value: " + ChatColor.GREEN + "$" + Utils.format(guiSubject.getTotalValue()),
				"Average account value: " + ChatColor.GREEN + "$" +
						Utils.format(guiSubject.getAccounts().isEmpty()
								? BigDecimal.ZERO
								: guiSubject.getTotalValue().divide(BigDecimal.valueOf(guiSubject.getAccounts().size()), RoundingMode.HALF_EVEN)),
				"Equality score: " + BankUtils.getEqualityLore(guiSubject)
		);
	}

	private List<String> getCreationLore() {
		AccountConfig config = guiSubject.getAccountConfig();
		boolean reimburse = config.get(AccountConfig.Field.REIMBURSE_ACCOUNT_CREATION);
		return Arrays.asList(
				"Fee per chest: " + ChatColor.GREEN + config.getFormatted(AccountConfig.Field.ACCOUNT_CREATION_PRICE),
				"Reimbursed on removal: " + (reimburse ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No")
		);
	}

	private List<String> getBalanceRestrictionLore() {
		AccountConfig config = guiSubject.getAccountConfig();
		double minBalance = config.get(AccountConfig.Field.MINIMUM_BALANCE);
		double lowBalanceFee = config.get(AccountConfig.Field.LOW_BALANCE_FEE);
		boolean strikethrough = minBalance == 0;
		boolean payOnLowBalance = config.get(AccountConfig.Field.PAY_ON_LOW_BALANCE);
		return Utils.wordWrapAll(38,
				"Minimum balance: " + ChatColor.GREEN + "$" + Utils.format(minBalance),
				"Low balance fee: " + ChatColor.RED
						+ (strikethrough ? ChatColor.STRIKETHROUGH : "") + "$" + Utils.format(lowBalanceFee),
				"",
				"Interest " + (payOnLowBalance ? ChatColor.GREEN + "will" : ChatColor.RED + "will not")
						+ " continue " + ChatColor.GRAY + "to be paid out when the account balance is low."
		);
	}

	private List<String> getOfflinePayoutsLore() {
		AccountConfig config = guiSubject.getAccountConfig();
		int offlinePayouts = config.get(AccountConfig.Field.ALLOWED_OFFLINE_PAYOUTS);
		int offlineDecrement = config.get(AccountConfig.Field.OFFLINE_MULTIPLIER_DECREMENT);
		int beforeReset = config.get(AccountConfig.Field.ALLOWED_OFFLINE_PAYOUTS_BEFORE_RESET);
		List<String> lore = new ArrayList<>();
		lore.add("Accounts will " + (offlinePayouts == 0
				? ChatColor.RED + "not generate interest" + ChatColor.GRAY
				: "generate interest up to " + ChatColor.AQUA + offlinePayouts + ChatColor.GRAY
						+ String.format(" time%s", offlinePayouts == 1 ? "" : "s"))
				+ " while account holders are offline.");
		if (beforeReset != 0) {
			lore.add("");
			lore.add("Account multipliers will " + (offlineDecrement == 0
					? ChatColor.AQUA + "freeze" + ChatColor.GRAY
					: "decrease by " + ChatColor.AQUA + offlineDecrement + ChatColor.GRAY + " for every payout")
				+ " during this time.");
		}
		if (beforeReset > -1) {
			lore.add("");
			lore.add("Multipliers will reset " + (beforeReset == 0
					? ChatColor.RED + "before paying out interest" + ChatColor.GRAY
					: "after generating interest " + ChatColor.AQUA + beforeReset + ChatColor.GRAY
					+ String.format(" time%s", beforeReset == 1 ? "" : "s")) + " while account holders are offline.");
		}
		return Utils.wordWrapAll(lore);
	}

	private List<String> getInterestRateLore() {
		double interestRate = guiSubject.getAccountConfig().get(AccountConfig.Field.INTEREST_RATE);
		return Utils.wordWrapAll(
			"" + ChatColor.GREEN + ChatColor.BOLD + String.format("%,.1f", interestRate * 100) + "%"
		);
	}

	private List<String> getInterestDelayLore() {
		AccountConfig config = guiSubject.getAccountConfig();
		int interestDelay = config.get(AccountConfig.Field.INITIAL_INTEREST_DELAY);
		boolean countOffline = config.get(AccountConfig.Field.COUNT_INTEREST_DELAY_OFFLINE);
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
		return Utils.wordWrapAll(lore);
	}

	private List<String> getWithdrawalPolicyLore() {
		int withdrawalDecrement = guiSubject.getAccountConfig().get(AccountConfig.Field.WITHDRAWAL_MULTIPLIER_DECREMENT);
		return Utils.wordWrapAll(
				"Account multipliers will " + (withdrawalDecrement == 0
						? ChatColor.GREEN + "not be affected on" + ChatColor.GRAY
						: "decrease by " + ChatColor.AQUA + withdrawalDecrement + ChatColor.GRAY
							+ String.format(" stage%s upon each", withdrawalDecrement == 1 ? "" : "s")) + " withdrawal."
		);
	}

	private List<String> getAccountLimitLore() {
		int accountLimit = guiSubject.getAccountConfig().get(AccountConfig.Field.PLAYER_BANK_ACCOUNT_LIMIT);
		return Utils.wordWrapAll(
				(accountLimit == 0
						? "Account creation is currently " + ChatColor.RED + "disabled" + ChatColor.GRAY
						: "Players may create " + (accountLimit > 0
								? "up to " + ChatColor.AQUA + accountLimit
								: ChatColor.GREEN + "unlimited") + ChatColor.GRAY
						+ String.format(" account%s at this bank.", accountLimit == 1 ? "" : "s"))
		);
	}

	private List<String> getPayoutTimeLore() {
		List<LocalTime> times = guiSubject.getAccountConfig().get(AccountConfig.Field.INTEREST_PAYOUT_TIMES);
		List<String> lore = new ArrayList<>();
		if (!times.isEmpty()) {
			lore.add("Accounts will generate interest every day at: ");
			for (LocalTime time : times)
				lore.add(ChatColor.GOLD + " - " + time.toString());
		} else
			lore.add("Accounts will not generate interest.");
		return Utils.wordWrapAll(lore);
	}
}
