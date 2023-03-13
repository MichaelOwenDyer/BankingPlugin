package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.Permissions;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.lang.ColorStringBuilder;
import com.monst.bankingplugin.util.Observer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BankGUI extends SinglePageGUI implements Observer {
	
	private final Bank bank;
	private boolean pendingUpdate;

	public BankGUI(BankingPlugin plugin, Player player, Bank bank) {
		super(plugin, player);
		this.bank = bank;
	}
	
	@Override
	public void onClose() {
		super.onClose();
		bank.unsubscribe(this);
	}
	
	@Override
	public void update() {
		pendingUpdate = !visible;
		if (pendingUpdate)
			return;
		createItems(player).forEach(inventory::setItem);
	}
	
	@Override
	void reopen() {
		visible = true;
		if (pendingUpdate) {
			pendingUpdate = false;
			createItems(player).forEach(inventory::setItem);
		}
		player.openInventory(inventory);
	}
	
	@Override
	Inventory createInventory() {
		return Bukkit.createInventory(this, 2 * 9, bank.getColorizedName());
	}
	
	@Override
	public void click(int slot, ClickType type) {
		if (slot == 0)
			handleClickTeleport(type);
		else if (slot == 7)
			handleClickIncomeLog();
		else if (slot == 8)
			handleClickAccountList();
	}
	
	private void handleClickTeleport(ClickType click) {
		if (!canTP(player))
			return;
		if (click.isLeftClick())
			teleport(player, bank.getRegion().getTeleportLocation());
		else if (click.isRightClick())
			teleport(player, bank.getRegion().getHighestTeleportLocation());
		exit();
	}
	
	private void handleClickIncomeLog() {
		if (isTrusted(player))
			child(new BankIncomeGUI(plugin, player, bank)).open();
	}
	
	private void handleClickAccountList() {
		if (isTrusted(player) && bank.getNumberOfAccounts() > 0)
			child(new BankAccountsGUI(plugin, player, bank)).open();
	}
	
	private boolean canTP(Player player) {
		return player.isOp()
				|| player.hasPermission("minecraft.command.tp")
				|| player.hasPermission("essentials.tp.position");
	}
	
	private boolean isTrusted(Player player) {
		return bank.isTrusted(player) || Permissions.ACCOUNT_LIST_OTHER.ownedBy(player);
	}
	
	@Override
	Map<Integer, ItemStack> createItems(Player player) {
		boolean isTrusted = isTrusted(player);
		Map<Integer, ItemStack> items = new HashMap<>();
		items.put(0, head(bank.getOwner(), "Bank Information", getGeneralInfoLore(canTP(player))));
		items.put(7, item(Material.BOOKSHELF, "Income Log", getIncomeLogLore(isTrusted)));
		items.put(8, item(Material.CHEST, "Account List", getAccountListLore(isTrusted)));
		items.put(9, item(Material.ENCHANTED_BOOK, "Account Creation", getCreationLore()));
		items.put(10, item(Material.NETHER_STAR, "Account Interest Multipliers", getMultipliersLore()));
		items.put(11, item(Material.IRON_BARS, "Minimum Account Balance", getMinimumBalanceLore()));
		items.put(12, item(Material.RED_BED, "Offline Payouts", getOfflinePayoutsLore()));
		items.put(13, item(Material.DIAMOND, "Interest Rate", getInterestRateLore()));
		items.put(14, item(Material.HOPPER, "Withdrawal Policy", getWithdrawalPolicyLore()));
		items.put(15, item(Material.TOTEM_OF_UNDYING, "Account Limit", getAccountLimitLore()));
		items.put(16, item(Material.CLOCK, "Interest Payout Times", getPayoutTimeLore()));
		items.put(17, item(Material.CAKE, "Statistics", getStatisticsLore()));
		return items;
	}
	
	private List<String> getGeneralInfoLore(boolean canTP) {
		Stream.Builder<String> lore = Stream.builder();
		lore.add("Bank ID: " + bank.getID());
		if (bank.isPlayerBank())
			lore.add("Owner: " + ChatColor.GOLD + bank.getOwner().getName());
		if (bank.hasCoOwners())
			lore.add("Co-owners: " + ChatColor.AQUA + bank.getCoOwners().stream()
					.map(OfflinePlayer::getName)
					.collect(Collectors.joining(", ")));
		lore.add("Location: " + ChatColor.AQUA + bank.getRegion().toString());
		if (canTP)
			lore.add("Click to teleport to bank.");
		return wordWrapAll(45, lore.build());
	}
	
	private List<String> getIncomeLogLore(boolean isTrusted) {
		if (!isTrusted)
			return NO_PERMISSION;
		return Collections.singletonList("Click to view income log.");
	}
	
	private List<String> getAccountListLore(boolean isTrusted) {
		if (!isTrusted)
			return NO_PERMISSION;
		if (bank.getNumberOfAccounts() > 0)
			return Collections.singletonList("Click here to view accounts.");
		return Collections.singletonList("There are no accounts at this bank.");
	}

	private List<String> getStatisticsLore() {
		return Arrays.asList(
				"Total account value: " + formatAndColorize(bank.getTotalValue()),
				"Average account value: " + formatAndColorize(bank.getAverageValue()),
				"Number of accounts: " + ChatColor.AQUA + bank.getNumberOfAccounts(),
				"Number of account holders: " + ChatColor.AQUA + bank.getAccountHolders().size(),
				"Equality score: " + getEqualityLore(bank)
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
		boolean reimburse = plugin.config().reimburseAccountCreation.at(bank);
		return Arrays.asList(
				"Fee per chest: " + ChatColor.GREEN + plugin.config().accountCreationPrice.at(bank),
				"Reimbursed on removal: " + (reimburse ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No")
		);
	}

	private List<String> getMultipliersLore() {
		List<Integer> interestMultipliers = plugin.config().interestMultipliers.at(bank);
		if (interestMultipliers.isEmpty())
			return Collections.singletonList(ChatColor.GREEN + "1x");
		
		List<List<Integer>> collapsedMultipliers = new ArrayList<>();
		collapsedMultipliers.add(new ArrayList<>());
		collapsedMultipliers.get(0).add(interestMultipliers.get(0));
		int level = 0;
		for (int i = 1; i < interestMultipliers.size(); i++) {
			if (interestMultipliers.get(i).equals(collapsedMultipliers.get(level).get(0)))
				collapsedMultipliers.get(level).add(interestMultipliers.get(i));
			else {
				collapsedMultipliers.add(new ArrayList<>());
				collapsedMultipliers.get(++level).add(interestMultipliers.get(i));
			}
		}
		
		List<String> lore = new ArrayList<>();
		
		int lower = 0;
		int upper = collapsedMultipliers.size();
		for (int i = lower; i < upper; i++) {
			ColorStringBuilder line = new ColorStringBuilder();
			
			line.gold(" - ", collapsedMultipliers.get(i).get(0), "x");
			
			int levelSize = collapsedMultipliers.get(i).size();
			if (levelSize > 1)
				line.darkGray(" (").gray("x").aqua(levelSize).darkGray(")");
			lore.add(line.toString());
		}
		if (upper < collapsedMultipliers.size())
			lore.add("...");
		return lore;
	}

	private List<String> getMinimumBalanceLore() {
		BigDecimal minBalance = plugin.config().minimumBalance.at(bank);
		BigDecimal lowBalanceFee = plugin.config().lowBalanceFee.at(bank);
		boolean strikethrough = minBalance.signum() == 0;
		boolean payOnLowBalance = plugin.config().payOnLowBalance.at(bank);
		Stream.Builder<String> lore = Stream.builder();
		lore.add("Minimum balance: " + ChatColor.GREEN + format(minBalance));
		lore.add("Low balance fee: " + ChatColor.RED + (strikethrough ? ChatColor.STRIKETHROUGH : "") + format(lowBalanceFee));
		if (!strikethrough) {
			lore.add("");
			lore.add("Interest " + (payOnLowBalance ? ChatColor.GREEN + "will" : ChatColor.RED + "will not")
					+ " continue " + ChatColor.GRAY + "to be paid out when the account balance is low.");
		}
		return wordWrapAll(38, lore.build());
	}

	private List<String> getOfflinePayoutsLore() {
		int offlinePayouts = plugin.config().allowedOfflinePayouts.at(bank);
		int offlineDecrement = plugin.config().offlineMultiplierDecrement.at(bank);
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
			ChatColor.GREEN + plugin.config().interestRate.toStringAt(bank)
		);
	}

	private List<String> getWithdrawalPolicyLore() {
		int withdrawalDecrement = plugin.config().withdrawalMultiplierDecrement.at(bank);
		return wordWrapAll(
				"Account multipliers will " + (withdrawalDecrement == 0
						? ChatColor.GREEN + "not be affected on" + ChatColor.GRAY
						: "decrease by " + ChatColor.AQUA + withdrawalDecrement + ChatColor.GRAY
							+ String.format(" stage%s upon each", withdrawalDecrement == 1 ? "" : "s")) + " withdrawal."
		);
	}

	private List<String> getAccountLimitLore() {
		int accountLimit = plugin.config().playerAccountPerBankLimit.at(bank);
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
		Set<LocalTime> times = plugin.config().interestPayoutTimes.at(bank);
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
