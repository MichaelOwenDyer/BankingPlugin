package com.monst.bankingplugin.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.Arrays;
import java.util.List;

public class Utils {

	public static boolean startsWithIgnoreCase(String string, String prefix) {
		return StringUtil.startsWithIgnoreCase(string, prefix);
	}

	public static boolean containsIgnoreCase(String string, String contained) {
		return string.toLowerCase().contains(contained.toLowerCase());
	}

	public static void message(OfflinePlayer player, String message) {
		if (player.getPlayer() != null)
			player.getPlayer().sendMessage(message);
	}

	@SuppressWarnings("deprecation")
	public static boolean isTransparent(Block block) {
		return isChest(block)
				|| block.getBlockData() instanceof Slab
				|| block.getBlockData() instanceof Stairs
				|| block.getType().isTransparent();
	}

	public static boolean isChest(Block block) {
		return block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST;
	}

	@SuppressWarnings("deprecation")
	public static OfflinePlayer getPlayer(String name) {
		OfflinePlayer player = Bukkit.getPlayerExact(name);
		if (player == null)
			player = Bukkit.getOfflinePlayer(name);
		return player.hasPlayedBefore() ? player : null;
	}

	/**
	 * @param player Player to check
	 * @return Whether the player is holding an axe in one of their hands
	 */
	public static boolean hasAxeInHand(Player player) {
		List<Material> axes = Arrays.asList(
				Material.WOODEN_AXE,
				Material.STONE_AXE,
				Material.IRON_AXE,
				Material.GOLDEN_AXE,
				Material.DIAMOND_AXE
		);
		return axes.contains(player.getInventory().getItemInMainHand().getType())
				|| axes.contains(player.getInventory().getItemInOffHand().getType());
	}

}
