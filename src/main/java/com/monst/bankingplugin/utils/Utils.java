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
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Utils {

	private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	public static String formatTime(Object toFormat) {
		return FORMATTER.format(toFormat);
	}

	public static boolean isAllowedName(String name) {
		try {
			return Config.nameRegex.get().trim().isEmpty() || Pattern.matches(Config.nameRegex.get(), name);
		} catch (PatternSyntaxException e) {
			return true;
		}
	}

	public static String removePunctuation(String s, char... exclude) {
		StringBuilder regex = new StringBuilder("[\\p{Punct}");
		if (exclude.length > 0) {
			regex.append("&&[^");
			for (char c : exclude)
				regex.append(c);
			regex.append("]");
		}
		regex.append("]");
		return s.replaceAll(regex.toString(), "");
	}

	public static String colorize(String s) {
		if (s == null)
			return null;
		return ChatColor.translateAlternateColorCodes('&', s);
	}

	public static String stripColor(String s) {
		return ChatColor.stripColor(colorize(s));
	}

	public static String format(double d) {
		return BankingPlugin.getInstance().getEconomy().format(d);
	}

	public static String format(BigDecimal bd) {
		return format(bd.doubleValue());
	}

	public static String formatAndColorize(BigDecimal bd) {
		ChatColor color = bd.signum() >= 1 ? ChatColor.GREEN : ChatColor.RED;
		return color + format(bd);
	}

	public static Location blockifyLocation(Location loc) {
		return new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}

	public static <T extends Comparable<T>> T lesser(T first, T second) {
		return BinaryOperator.minBy(T::compareTo).apply(first, second);
	}

	public static <T extends Comparable<T>> T greater(T first, T second) {
		return BinaryOperator.maxBy(T::compareTo).apply(first, second);
	}

	public static Integer parseInteger(String s) {
		return catchRuntimeException(() -> Integer.parseInt(s));
	}

	public static Double parseDouble(String s) {
		return catchRuntimeException(() -> Double.parseDouble(s));
	}

	public static LocalTime parseLocalTime(String s) {
		return catchRuntimeException(() -> LocalTime.parse(s));
	}

	public static <T> T catchRuntimeException(Supplier<T> supplier) {
		try {
			return supplier.get();
		} catch (RuntimeException e) {
			return null;
		}
	}

	public static boolean startsWithIgnoreCase(String s1, String s2) {
		return s1.toLowerCase(Locale.ROOT).startsWith(s2.toLowerCase(Locale.ROOT));
	}

	public static boolean containsIgnoreCase(String s1, String s2) {
		return s1.toLowerCase(Locale.ROOT).contains(s2.toLowerCase(Locale.ROOT));
	}

	public static void teleport(Player player, Location location) {
		player.teleport(location.setDirection(player.getLocation().getDirection()));
	}

	/**
	 * Finds the next lowest safe location at or directly below a certain {@link Location}.
	 * If no safe block is found then the original location is returned.
	 * @param location the location from which to start searching
	 * @return a {@link Location} at or directly below the given location that is safe to stand on
	 */
	public static Location getSafeLocation(Location location) {
		World world = location.getWorld();
		if (world == null)
			return location;
		int blockX = location.getBlockX();
		int blockZ = location.getBlockZ();
		for (int y = location.getBlockY(); y > 0; y--)
			if (isSafeBlock(world.getBlockAt(blockX, y, blockZ)))
				return new Location(world, blockX + 0.5d, y, blockZ + 0.5d);
		return location;
	}

	/**
	 * Checks if a {@link Block} is safe to stand on (solid ground with 2 breathable blocks)
	 *
	 * @param b Block to check
	 * @return true if block is safe
	 */
	@SuppressWarnings("deprecation")
	public static boolean isSafeBlock(Block b) {
		if (!b.getType().isTransparent() && !b.getLocation().add(0, 1, 0).getBlock().getType().isTransparent()) {
			return false; // not transparent (standing in block)
		}
		if (!b.getRelative(BlockFace.UP).getType().isTransparent()) {
			return false; // not transparent (will suffocate)
		}
		return b.getRelative(BlockFace.DOWN).getType().isSolid() || b.getRelative(BlockFace.DOWN).getType() == Material.WATER;
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

	public static BukkitTask runTaskLater(Runnable runnable, long delay) {
		return Utils.bukkitRunnable(runnable).runTaskLater(BankingPlugin.getInstance(), delay);
	}

	@Nonnull
	public static <T> T nonNull(@Nullable T ifNotNull, T ifNull) {
		return ifNotNull != null ? ifNotNull : ifNull;
	}

	@Nonnull
	public static <T> T nonNull(@Nullable T ifNotNull, @Nonnull Supplier<T> ifNull) {
		return ifNotNull != null ? ifNotNull : ifNull.get();
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
	public static <T> List<List<T>> stackList(List<T> list) {
		List<List<T>> stackedList = new ArrayList<>();
		stackedList.add(new ArrayList<>());
		stackedList.get(0).add(list.get(0));
		int level = 0;
		for (int i = 1; i < list.size(); i++) {
			if (Objects.equals(list.get(i), stackedList.get(level).get(0)))
				stackedList.get(level).add(list.get(i));
			else {
				stackedList.add(new ArrayList<>());
				stackedList.get(++level).add(list.get(i));
			}
		}
		return stackedList;
	}

	public static boolean samePlayer(OfflinePlayer p1, OfflinePlayer p2) {
		if (p1 == null || p2 == null)
			return false;
		return Objects.equals(p1.getUniqueId(), p2.getUniqueId());
	}

	@SuppressWarnings("deprecation")
	public static OfflinePlayer getPlayer(String name) {
		OfflinePlayer player = nonNull(Bukkit.getPlayerExact(name), () -> Bukkit.getOfflinePlayer(name));
		return player.hasPlayedBefore() ? player : null;
	}

	public static OfflinePlayer getPlayer(UUID uuid) {
		OfflinePlayer player = nonNull(Bukkit.getPlayer(uuid), () -> Bukkit.getOfflinePlayer(uuid));
		return player.hasPlayedBefore() ? player : null;
	}

	public static OfflinePlayer getPlayerFromUUID(String uuid) {
		return getPlayerFromUUID(UUID.fromString(uuid));
	}

	public static OfflinePlayer getPlayerFromUUID(UUID uuid) {
		return Bukkit.getOfflinePlayer(uuid);
	}

	public static List<String> getOnlinePlayerNames(BankingPlugin plugin) {
		return plugin.getServer().getOnlinePlayers().stream()
				.map(HumanEntity::getName)
				.sorted()
				.collect(Collectors.toList());
	}

	public static int getAccountLimit(Player player) {
		return (int) getLimit(player, Permissions.ACCOUNT_NO_LIMIT, Config.defaultAccountLimit.get());
	}

	public static int getBankLimit(Player player) {
		return (int) getLimit(player, Permissions.BANK_NO_LIMIT, Config.defaultBankLimit.get());
	}

	/**
	 * Gets the bank volume limit of a certain player, to see if the player is allowed to create a bank of a certain size.
	 */
	public static long getBankVolumeLimit(Player player) {
		return getLimit(player, Permissions.BANK_NO_SIZE_LIMIT, Config.maximumBankVolume.get());
	}

	private static long getLimit(Player player, String permission, long defaultLimit) {
		long limit = 0;
		boolean useDefault = true;

		String permPrefix = permission.replaceAll("\\*", "");

		for (PermissionAttachmentInfo permInfo : player.getEffectivePermissions()) {
			if (permInfo.getPermission().startsWith(permPrefix)
					&& player.hasPermission(permInfo.getPermission())) {
				if (permInfo.getPermission().equalsIgnoreCase(permission)) {
					limit = -1;
					useDefault = false;
					break;
				} else {
					String[] spl = permInfo.getPermission().split(permPrefix);

					if (spl.length > 1) {
						try {
							long newLimit = Long.parseLong(spl[1]);
							if (newLimit < 0) {
								limit = -1;
								break;
							}
							limit = Math.max(limit, newLimit);
							useDefault = false;
						} catch (NumberFormatException ignored) {}
					}
				}
			}
		}
		if (limit < -1)
			limit = -1;
		return useDefault ? defaultLimit : limit;
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
	 * @param p Player whose secondary held item should be returned
	 * @return the {@link ItemStack} in the player's off hand, or {@code null} if they player isn't holding anything
	 * in their off hand or the server version is below 1.9
	 */
	public static ItemStack getItemInOffHand(Player p) {
		if (getMajorVersion() < 9)
			return null;
		if (p.getInventory().getItemInOffHand().getType() == Material.AIR)
			return null;
		return p.getInventory().getItemInOffHand();
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

		ItemStack item = getItemInMainHand(p);
		if (item == null || !axes.contains(item.getType().toString())) {
			item = getItemInOffHand(p);
		}

		return item != null && axes.contains(item.getType().toString());
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
