package com.monst.bankingplugin.utils;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.monst.bankingplugin.Account;
import com.monst.bankingplugin.BankingPlugin;

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

	/**
	 * @param time Double representation of a time during the day
	 * @param hourFormat Whether or not to enable the 12-hour AM/PM format
	 * @return A string representing the time
	 */
	public static String convertDoubleTime(double time, boolean hourFormat) {
		
		time = (time % 24 + 24) % 24;
		
		int hour = (int) time;
		int minute = (int) ((time - hour) * 60);
		
		String minutes = minute < 10 ? "0" + minute : "" + minute;

		if (hourFormat) {
			String suffix = "AM";
			if (hour >= 13) {
				hour -= 12;
				suffix = "PM";
			} else
				if (hour == 12)
					suffix = "PM";
				else if (hour == 0)
					hour += 12;
			
			return hour + ":" + minutes + " " + suffix;
		} else
			return hour + ":" + minutes;
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
