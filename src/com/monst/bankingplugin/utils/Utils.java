package com.monst.bankingplugin.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.Bank;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.utils.AccountConfig.Field;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

public class Utils {

	public static Location blockifyLocation(Location location) {
		return new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}
	
	public static List<String> getOnlinePlayerNames(BankingPlugin plugin) {
		return plugin.getServer().getOnlinePlayers().stream().map(player -> player.getName()).sorted()
				.collect(Collectors.toList());
	}

	public static boolean isAllowedName(String name) {
		try {
			return Config.nameRegex.contentEquals("") || name.matches(Config.nameRegex);
		} catch (PatternSyntaxException e) {
			return true;
		}
	}

	public static String colorize(String s) {
		if (s == null)
			return null;
		return s.replaceAll("&([0-9a-f])", "\u00A7$1");
	}

	public static String stripColor(String s) {
		return ChatColor.stripColor(colorize(s));
	}

	public static String formatNumber(double d) {
		return formatNumber(BigDecimal.valueOf(d));
	}

	public static String formatNumber(BigDecimal bd) {
		return String.format("%,.2f", bd);
	}

	public static String removePunctuation(String list) {
		return list.replaceAll("\\p{Punct}", "");
	}

	public static String listifyList(String list) {
		return "[" + removePunctuation(list).replace(" ", ", ") + "]";
	}
	
	@SuppressWarnings("deprecation")
	public static boolean isTransparent(Block block) {
		return (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST
				|| block.getBlockData() instanceof Slab || block.getBlockData() instanceof Stairs
				|| block.getType().isTransparent());
    }

	private static List<List<Integer>> getStackedList(List<Integer> multipliers) {
		List<List<Integer>> stackedMultipliers = new ArrayList<>();
		stackedMultipliers.add(new ArrayList<>());
		stackedMultipliers.get(0).add(multipliers.get(0));
		int level = 0;
		for (int i = 1; i < multipliers.size(); i++) {
			if (multipliers.get(i) == stackedMultipliers.get(level).get(0))
				stackedMultipliers.get(level).add(multipliers.get(i));
			else {
				stackedMultipliers.add(new ArrayList<>());
				stackedMultipliers.get(++level).add(multipliers.get(i));
			}
		}
		return stackedMultipliers;
	}

	public static TextComponent getMultiplierView(Bank bank) {
		return getMultiplierView(bank, -1);
	}

	public static TextComponent getMultiplierView(Account account) {
		return getMultiplierView(account.getBank(), account.getStatus().getMultiplierStage());
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	private static TextComponent getMultiplierView(Bank bank, int highlightStage) {

		List<Integer> multipliers = (List<Integer>) bank.getAccountConfig().getOrDefault(Field.MULTIPLIERS);

		TextComponent multiplierView = new TextComponent();
		multiplierView.setColor(net.md_5.bungee.api.ChatColor.GRAY);

		if (multipliers.size() == 0) {
			multiplierView.setText(ChatColor.GREEN + "1x");
			return multiplierView;
		}

		List<List<Integer>> stackedMultipliers = Utils.getStackedList(multipliers);

		final int listSize = 5;
		int stage = -1;
		if (highlightStage != -1)
			for (List<Integer> list : stackedMultipliers) {
				stage++;
				if (highlightStage - list.size() < 0)
					break;
				else
					highlightStage -= list.size();
			}

		TextComponent openingBracket = new TextComponent(ChatColor.GOLD + "[");
		openingBracket.setBold(true);
		TextComponent closingBracket = new TextComponent(ChatColor.GOLD + " ]");
		closingBracket.setBold(true);
		TextComponent ellipses = new TextComponent(" ...");

		multiplierView.addExtra(openingBracket);

		int lower = 0;
		int upper = stackedMultipliers.size();

		if (stage != -1 && stackedMultipliers.size() > listSize) {
			lower = stage - (listSize / 2);
			upper = stage + (listSize / 2) + 1;
			while (lower < 0) {
				lower++;
				upper++;
			}
			while (upper > stackedMultipliers.size()) {
				lower--;
				upper--;
			}

			if (lower > 0)
				multiplierView.addExtra(ellipses);
		}

		for (int i = lower; i < upper; i++) {
			TextComponent number = new TextComponent(" " + stackedMultipliers.get(i).get(0) + "x");

			if (i == stage) {
				number.setColor(net.md_5.bungee.api.ChatColor.GREEN);
				number.setBold(true);
			}
			int levelSize = stackedMultipliers.get(i).size();
			if (levelSize > 1) {
				number.setBold(true);
				ComponentBuilder cb = new ComponentBuilder();
				if (stage == -1 || i < stage) {
					cb.append("" + ChatColor.GREEN + levelSize).append(ChatColor.DARK_GRAY + "/")
							.append("" + ChatColor.GREEN + levelSize);
				} else if (i > stage) {
					cb.append("0").color(net.md_5.bungee.api.ChatColor.RED).append("/")
							.color(net.md_5.bungee.api.ChatColor.DARK_GRAY).append("" + levelSize)
							.color(net.md_5.bungee.api.ChatColor.GREEN);
				} else {
					net.md_5.bungee.api.ChatColor color;
					if (highlightStage == levelSize - 1)
						color = net.md_5.bungee.api.ChatColor.GREEN;
					else if (highlightStage > (levelSize - 1) / 2)
						color = net.md_5.bungee.api.ChatColor.GOLD;
					else
						color = net.md_5.bungee.api.ChatColor.RED;

					cb.append("" + highlightStage).color(color).append("/")
							.color(net.md_5.bungee.api.ChatColor.DARK_GRAY).append("" + levelSize)
							.color(net.md_5.bungee.api.ChatColor.GREEN);
				}
				number.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, cb.create()));
			}
			multiplierView.addExtra(number);
		}
		if (upper < stackedMultipliers.size())
			multiplierView.addExtra(ellipses);
		multiplierView.addExtra(closingBracket);
		return multiplierView;
	}

	@SuppressWarnings("deprecation")
	public static TextComponent getInterestRateView(Account account) {
		if (account == null)
			return null;

		AccountStatus accountStatus = account.getStatus();
		AccountConfig accountConfig = account.getBank().getAccountConfig();

		TextComponent interestRateView = new TextComponent();
		interestRateView.setColor(net.md_5.bungee.api.ChatColor.GREEN);

		double baseInterestRate = (double) accountConfig.getOrDefault(Field.INTEREST_RATE);
		double percentage = baseInterestRate * accountStatus.getRealMultiplier();
		TextComponent interestRate = new TextComponent(percentage * 100 + "%");
		if (accountStatus.getDelayUntilNextPayout() == 0
				&& account.getBalance().doubleValue() >= accountConfig.getMinBalance()) {
			ComponentBuilder cb = new ComponentBuilder();
			cb.append("Next payout: ").color(net.md_5.bungee.api.ChatColor.GRAY)
					.append("$" + Utils.formatNumber(account.getBalance().multiply(BigDecimal.valueOf(percentage))))
					.color(net.md_5.bungee.api.ChatColor.GREEN);
			interestRate.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, cb.create()));
		}
		interestRate
				.addExtra(ChatColor.GRAY + " (" + baseInterestRate + " x " + accountStatus.getRealMultiplier() + ")");
		interestRateView.addExtra(interestRate);

		return interestRateView;
	}

	/**
	 * Calculates Gini coefficient of this bank. This is a measurement of wealth
	 * inequality among all n accounts at the bank.
	 * 
	 * @return G = ( 2 * sum(i,n)(i * value of ith account) / n * sum(i,n)(value of
	 *         ith account) ) - ( n + 1 / n )
	 */
	public static double getGiniCoefficient(Bank bank) {
		if (bank.getAccounts().isEmpty())
			return 0;
		List<BigDecimal> orderedValues = bank.getCustomerBalances().values().stream().sorted(BigDecimal::compareTo)
				.collect(Collectors.toList());
		BigDecimal valueSum = BigDecimal.ZERO;
		BigDecimal weightedValueSum = BigDecimal.ZERO;
		for (int i = 0; i < orderedValues.size(); i++) {
			valueSum = valueSum.add(orderedValues.get(i));
			weightedValueSum = weightedValueSum.add(orderedValues.get(i).multiply(BigDecimal.valueOf(i + 1)));
		}
		valueSum = valueSum.multiply(BigDecimal.valueOf(orderedValues.size()));
		weightedValueSum = weightedValueSum.multiply(BigDecimal.valueOf(2));
		if (valueSum.signum() == 0)
			return 0;
		BigDecimal leftEq = weightedValueSum.divide(valueSum, 10, RoundingMode.HALF_EVEN);
		BigDecimal rightEq = BigDecimal.valueOf((orderedValues.size() + 1) / orderedValues.size());
		BigDecimal gini = leftEq.subtract(rightEq).setScale(2, RoundingMode.HALF_EVEN);
		return gini.doubleValue();
	}

	public static TextComponent getEqualityView(Bank bank) {
		
		double gini = getGiniCoefficient(bank);
		TextComponent equalityView = new TextComponent(String.format("%.2f", gini));
		
		switch ((int) (gini * 5d)) {
		case 0:
			equalityView.setColor(net.md_5.bungee.api.ChatColor.DARK_GREEN);
			break;
		case 1:
			equalityView.setColor(net.md_5.bungee.api.ChatColor.GREEN);
			break;
		case 2:
			equalityView.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
			break;
		case 3:
			equalityView.setColor(net.md_5.bungee.api.ChatColor.RED);
			break;
		case 4: case 5:
			equalityView.setColor(net.md_5.bungee.api.ChatColor.DARK_RED);
			break;
		}

		return equalityView;
	}

    /**
     * @param p Player whose item in his main hand should be returned
     * @return {@link ItemStack} in his main hand, or {@code null} if he doesn't hold one
     */
	@SuppressWarnings("deprecation")
	public static ItemStack getItemInMainHand(Player p) {
        if (getMajorVersion() < 9) {
            if (p.getItemInHand().getType() == Material.AIR)
                return null;
            else
                return p.getItemInHand();
        }

        if (p.getInventory().getItemInMainHand().getType() == Material.AIR)
            return null;
        else
            return p.getInventory().getItemInMainHand();
    }

    /**
     * @param p Player whose item in his off hand should be returned
     * @return {@link ItemStack} in his off hand, or {@code null} if he doesn't hold one or the server version is below 1.9
     */
    public static ItemStack getItemInOffHand(Player p) {
        if (getMajorVersion() < 9)
            return null;
        else if (p.getInventory().getItemInOffHand().getType() == Material.AIR)
            return null;
        else
            return p.getInventory().getItemInOffHand();
    }

    /**
     * @param p Player to check if he has an axe in one of his hands
     * @return Whether a player has an axe in one of his hands
     */
    public static boolean hasAxeInHand(Player p) {
        List<String> axes;
        if (Utils.getMajorVersion() < 13)
            axes = Arrays.asList("WOOD_AXE", "STONE_AXE", "IRON_AXE", "GOLD_AXE", "DIAMOND_AXE");
        else 
            axes = Arrays.asList("WOODEN_AXE", "STONE_AXE", "IRON_AXE", "GOLDEN_AXE", "DIAMOND_AXE");

        ItemStack item = getItemInMainHand(p);
        if (item == null || !axes.contains(item.getType().toString())) {
            item = getItemInOffHand(p);
        }

        return item != null && axes.contains(item.getType().toString());
    }

 
    /**
     * Get a set for the location(s) of the shop's chest(s)
     * @param shop The shop
     * @return A set of 1 or 2 locations
     */
    public static Set<Location> getChestLocations(Account account) {
        Set<Location> chestLocations = new HashSet<>();
        InventoryHolder ih = account.getInventoryHolder();
        if (ih instanceof DoubleChest) {
            DoubleChest dc = (DoubleChest) ih;
            chestLocations.add(((Chest) dc.getLeftSide()).getLocation());
            chestLocations.add(((Chest) dc.getRightSide()).getLocation());
		} else
            chestLocations.add(account.getLocation());
        return chestLocations;
    }

    /**
     * @return The current server version with revision number (e.g. v1_9_R2, v1_10_R1)
     */
    public static String getServerVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();

        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    /**
     * @return The major version of the server (e.g. <i>9</i> for 1.9.2, <i>10</i> for 1.10)
     */
    public static int getMajorVersion() {
        return Integer.parseInt(getServerVersion().split("_")[1]);
    }

	public static String[] getVersionMessage() {
		return new String[] { 
				ChatColor.GREEN + "   __ " + ChatColor.DARK_GREEN + "  __",
				ChatColor.GREEN + "  |__)" + ChatColor.DARK_GREEN + " |__)   " + ChatColor.DARK_GREEN + "BankingPlugin" + ChatColor.AQUA + " v" + BankingPlugin.getInstance().getDescription().getVersion(),
				ChatColor.GREEN + "  |__)" + ChatColor.DARK_GREEN + " |   " + ChatColor.DARK_GRAY + "        by monst",
				"" };
	}

}
