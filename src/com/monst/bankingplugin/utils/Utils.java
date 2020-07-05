package com.monst.bankingplugin.utils;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import com.monst.bankingplugin.BankingPlugin;

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

	public static String simplifyList(String list) {
		return list.replaceAll("\\p{Punct}", "");
	}

	public static String listifyList(String list) {
		return "[" + simplifyList(list).replace(" ", ", ") + "]";
	}
	
	@SuppressWarnings("deprecation")
	public static boolean isTransparent(Block block) {
		return (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST
				|| block.getBlockData() instanceof Slab || block.getBlockData() instanceof Stairs
				|| block.getType().isTransparent());
    }

	public static List<List<Integer>> getStackedList(List<Integer> multipliers) {
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

	public static TextComponent getMultiplierView(List<Integer> multipliers) {
		return getMultiplierView(multipliers, -1);
	}

	public static TextComponent getMultiplierView(List<Integer> multipliers, int highlightStage) {

		TextComponent message = new TextComponent();
		message.setColor(net.md_5.bungee.api.ChatColor.GRAY);

		if (multipliers.size() == 0) {
			message.setText(ChatColor.GREEN + "1x");
			return message;
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

		TextComponent openingBracket = new TextComponent(ChatColor.GOLD + " [");
		openingBracket.setBold(true);

		message.addExtra(openingBracket);

		TextComponent closingBracket = new TextComponent(ChatColor.GOLD + " ]");
		closingBracket.setBold(true);

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
				message.addExtra(" ...");
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
			message.addExtra(number);
		}
		if (upper < stackedMultipliers.size())
			message.addExtra(" ...");
		message.addExtra(closingBracket);
		return message;
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
     * @param className Name of the class
     * @return Class in {@code net.minecraft.server.[VERSION]} package with the specified name or {@code null} if the class was not found
     */
    public static Class<?> getNMSClass(String className) {
        try {
            return Class.forName("net.minecraft.server." + getServerVersion() + "." + className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Send a packet to a player
     * @param plugin An instance of the {@link ShopChest} plugin
     * @param packet Packet to send
     * @param player Player to which the packet should be sent
     * @return {@code true} if the packet was sent, or {@code false} if an exception was thrown
     */
    public static boolean sendPacket(BankingPlugin plugin, Object packet, Player player) {
        try {
            if (packet == null) {
                plugin.debug("Failed to send packet: Packet is null");
                return false;
            }

            Class<?> packetClass = getNMSClass("Packet");
            if (packetClass == null) {
                plugin.debug("Failed to send packet: Could not find Packet class");
                return false;
            }

            Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);

            playerConnection.getClass().getMethod("sendPacket", packetClass).invoke(playerConnection, packet);

            return true;
        } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException e) {
            //plugin.getLogger().severe("Failed to send packet " + packet.getClass().getName());
            plugin.debug("Failed to send packet " + packet.getClass().getName());
            plugin.debug(e);
            return false;
        }
    }

    /**
     * @return The current server version with revision number (e.g. v1_9_R2, v1_10_R1)
     */
    public static String getServerVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();

        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    /**
     * @return The revision of the current server version (e.g. <i>2</i> for v1_9_R2, <i>1</i> for v1_10_R1)
     */
    public static int getRevision() {
        return Integer.parseInt(getServerVersion().substring(getServerVersion().length() - 1));
    }

    /**
     * @return The major version of the server (e.g. <i>9</i> for 1.9.2, <i>10</i> for 1.10)
     */
    public static int getMajorVersion() {
        return Integer.parseInt(getServerVersion().split("_")[1]);
    }

}
