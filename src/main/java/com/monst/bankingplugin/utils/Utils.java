package com.monst.bankingplugin.utils;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {

	private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public static String currentTimestamp() {
		return formatTime(Calendar.getInstance().getTime());
	}

	public static String formatTime(Object o) {
		return FORMATTER.format(o);
	}

	public static String colorize(String s) {
		if (s == null)
			return null;
		return ChatColor.translateAlternateColorCodes('&', s);
	}

	public static String format(double d) {
		return BankingPlugin.getInstance().getEconomy().format(d);
	}

	public static String format(BigDecimal bd) {
		return format(bd.doubleValue());
	}

	public static String formatAndColorize(BigDecimal bd) {
		ChatColor color = bd.signum() >= 0 ? ChatColor.GREEN : ChatColor.RED;
		return color + format(bd);
	}

	public static boolean startsWithIgnoreCase(String s1, String s2) {
		return s1.toLowerCase(Locale.getDefault()).startsWith(s2.toLowerCase(Locale.getDefault()));
	}

	public static boolean containsIgnoreCase(String s1, String s2) {
		return s1.toLowerCase(Locale.getDefault()).contains(s2.toLowerCase(Locale.getDefault()));
	}

	public static void teleport(Player player, Location location) {
		player.teleport(location.setDirection(player.getLocation().getDirection()));
	}

	public static void notify(OfflinePlayer player, String message) {
		if (player.isOnline())
			player.getPlayer().sendMessage(message);
	}

	/**
	 * Finds the next lowest safe {@link Block} at or directly below a certain {@link Block}.
	 * If no safe block is found then the original block is returned.
	 * @param start the block from which to start searching
	 * @return a {@link Block} at or directly below the given block that is safe to stand on
	 */
	public static Block getSafeBlock(Block start) {
		for (Block block = start; start.getY() > 0; block = block.getRelative(BlockFace.DOWN))
			if (isSafeBlock(block))
				return block;
		return start;
	}

	/**
	 * Checks if a {@link Block} is safe to stand on (solid ground with 2 breathable blocks)
	 *
	 * @param b Block to check
	 * @return true if block is safe
	 */
	@SuppressWarnings("deprecation")
	public static boolean isSafeBlock(Block b) {
		if (!b.getType().isTransparent())
			return false; // not transparent (standing in block)
		Block blockAbove = b.getRelative(BlockFace.UP);
		if (!blockAbove.getType().isTransparent())
			return false; // not transparent (will suffocate)
		Block blockBelow = b.getRelative(BlockFace.DOWN);
		return blockBelow.getType().isSolid() || blockBelow.getType() == Material.WATER;
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

	public static BukkitRunnable bukkitRunnable(Runnable runnable) {
		return new BukkitRunnable() {
			@Override
			public void run() {
				runnable.run();
			}
		};
	}

	/**
	 * Creates a list of lists of elements where each sub-list contains only equal elements which appeared
	 * consecutively in the original list. The order of the original list is preserved such that a flatMap
	 * operation would create the original list again.
	 *
	 * <p>For example, a list [0, 2, 2, 2, 4, 2, 1, 1] would be turned into a list [[0], [2, 2, 2], [4], [2], [1, 1]].
	 * @param list the list to be converted
	 * @param <T> the type of the elements of the list
	 * @return a stacked list
	 */
	public static <T> List<List<T>> collapseList(List<T> list) {
		List<List<T>> collapsedList = new ArrayList<>();
		if (list.isEmpty())
			return collapsedList;
		collapsedList.add(new ArrayList<>());
		collapsedList.get(0).add(list.get(0));
		int level = 0;
		for (int i = 1; i < list.size(); i++) {
			if (Objects.equals(list.get(i), collapsedList.get(level).get(0)))
				collapsedList.get(level).add(list.get(i));
			else {
				collapsedList.add(new ArrayList<>());
				collapsedList.get(++level).add(list.get(i));
			}
		}
		return collapsedList;
	}

	public static boolean samePlayer(OfflinePlayer p1, OfflinePlayer p2) {
		if (p1 == null || p2 == null)
			return false;
		return Objects.equals(p1.getUniqueId(), p2.getUniqueId());
	}

	@SuppressWarnings("deprecation")
	public static OfflinePlayer getPlayer(String name) {
		OfflinePlayer player = Bukkit.getPlayerExact(name);
		if (player == null)
			player = Bukkit.getOfflinePlayer(name);
		return player.hasPlayedBefore() ? player : null;
	}

	public static OfflinePlayer getPlayer(UUID uuid) {
		OfflinePlayer player = Bukkit.getPlayer(uuid);
		if (player == null)
			player = Bukkit.getOfflinePlayer(uuid);
		return player.hasPlayedBefore() ? player : null;
	}

	public static List<String> getOnlinePlayerNames() {
		return Bukkit.getServer().getOnlinePlayers().stream()
				.map(HumanEntity::getName)
				.sorted()
				.collect(Collectors.toList());
	}

	public static int getAccountLimit(Player player) {
		return (int) getLimit(player, Permission.ACCOUNT_NO_LIMIT, Config.defaultAccountLimit.get());
	}

	public static int getBankLimit(Player player) {
		return (int) getLimit(player, Permission.BANK_NO_LIMIT, Config.defaultBankLimit.get());
	}

	/**
	 * Gets the bank volume limit of a certain player, to see if the player is allowed to create a bank of a certain size.
	 */
	public static long getBankVolumeLimit(Player player) {
		return getLimit(player, Permission.BANK_NO_SIZE_LIMIT, Config.maximumBankVolume.get());
	}

	private static long getLimit(Player player, Permission unlimitedPerm, final long defaultLimit) {
		if (unlimitedPerm.ownedBy(player))
			return -1;
		long limit = defaultLimit;
		String permPrefix = unlimitedPerm.toString();
		permPrefix = permPrefix.substring(0, permPrefix.length() - 1);
		for (PermissionAttachmentInfo permInfo : player.getEffectivePermissions()) {
			if (!permInfo.getValue())
				continue;
			String[] split = permInfo.getPermission().split(permPrefix);
			if (split.length <= 1)
				continue;
			try {
				long newLimit = Long.parseLong(split[1]);
				if (newLimit < 0)
					return -1;
				limit = Math.max(limit, newLimit);
			} catch (NumberFormatException ignored) {}
		}
		return Math.max(limit, -1);
	}

	public static <T> Set<T> filter(Set<? extends T> collection, Predicate<? super T> filter) {
		return filter(collection, filter, Collectors.toCollection(HashSet::new));
	}
	public static <T> List<T> filter(List<? extends T> collection, Predicate<? super T> filter) {
		return filter(collection, filter, Collectors.toCollection(ArrayList::new));
	}

	/**
	 * Filters the elements of the provided collection and returns them in a {@link Collection} of the specified type
	 * @param collection the collection of elements to be filtered
	 * @param filter the filter to apply to each element
	 * @param collector the collector to collect the filtered elements with
	 * @param <T> the type of the elements in the collection
	 * @param <R> the type of collection the filtered elements should be returned in
	 * @return a {@link Collection} of the filtered elements
	 */
	public static <T, R> R filter(Collection<? extends T> collection, Predicate<? super T> filter, Collector<? super T, ?, R> collector) {
		if (collection == null)
			return null;
		return collection.stream().filter(filter).collect(collector);
	}

	public static <T, K> Set<K> map(Set<? extends T> collection, Function<? super T, ? extends K> mapper) {
		return map(collection, mapper, Collectors.toCollection(HashSet::new));
	}

	public static <T, K> List<K> map(List<? extends T> collection, Function<? super T, ? extends K> mapper) {
		return map(collection, mapper, Collectors.toCollection(ArrayList::new));
	}

	/**
	 * Maps the elements of the provided collection and returns them in a new {@link Collection} of the specified type.
	 * @param collection the collection of elements to be mapped
	 * @param mapper the mapper to apply to each element
	 * @param collector the collector to collect the mapped elements with
	 * @param <T> the type of the elements in the collection
	 * @param <K> the type of the elements after the mapping
	 * @param <R> the type of collection the mapped elements should be returned in
	 * @return a {@link Collection} of mapped elements
	 */
	public static <T, K, R> R map(Collection<? extends T> collection, Function<? super T, ? extends K> mapper, Collector<? super K, ?, R> collector) {
		if (collection == null)
			return null;
		return collection.stream().map(mapper).collect(collector);
	}

	/**
	 * @param p Player whose held item should be returned
	 * @return the {@link ItemStack} in the player's main hand, or {@code null} if the player isn't holding anything
	 * in their main hand
	 */
	@SuppressWarnings("deprecation")
	public static ItemStack getItemInMainHand(Player p) {
		ItemStack item = getMajorVersion() < 9 ? p.getItemInHand() : p.getInventory().getItemInMainHand();
		return item.getType() == Material.AIR ? null : item;
	}

	/**
	 * @param p Player whose secondary held item should be returned
	 * @return the {@link ItemStack} in the player's off hand, or {@code null} if the player isn't holding anything
	 * in their off hand or the server version is below 1.9
	 */
	public static ItemStack getItemInOffHand(Player p) {
		if (getMajorVersion() < 9)
			return null;
		ItemStack item = p.getInventory().getItemInOffHand();
		return item.getType() == Material.AIR ? null : item;
	}

	public static ItemStack[] getItemsInHands(Player p) {
		return Stream.of(getItemInMainHand(p), getItemInOffHand(p)).filter(Objects::nonNull).toArray(ItemStack[]::new);
	}

	/**
	 * @param p Player to check
	 * @return Whether the player is holding an axe in one of their hands
	 */
	public static boolean hasAxeInHand(Player p) {
		List<String> axes;
		if (Utils.getMajorVersion() < 13)
			axes = Arrays.asList("WOOD_AXE", "STONE_AXE", "IRON_AXE", "GOLD_AXE", "DIAMOND_AXE");
		else
			axes = Arrays.asList("WOODEN_AXE", "STONE_AXE", "IRON_AXE", "GOLDEN_AXE", "DIAMOND_AXE");
		return Arrays.stream(getItemsInHands(p))
				.map(ItemStack::getType)
				.map(Material::toString)
				.anyMatch(axes::contains);
	}

    /**
     * @return The current server version with revision number (e.g. v1_15_R1, v1_16_R2)
     */
    public static String getServerVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    /**
     * @return The major version of the server (e.g. <i>15</i> for 1.15.2, <i>16</i> for 1.16.2)
     */
    public static int getMajorVersion() {
        return Integer.parseInt(getServerVersion().split("_")[1]);
    }

}
