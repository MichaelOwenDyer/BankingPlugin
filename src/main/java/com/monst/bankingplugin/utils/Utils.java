package com.monst.bankingplugin.utils;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.exceptions.TransactionFailedException;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Utils {

	private static final NumberFormat integerFormatter = NumberFormat.getInstance();
	private static final NumberFormat decimalFormatter = NumberFormat.getInstance();
	static {
		decimalFormatter.setMinimumIntegerDigits(1);
		decimalFormatter.setMinimumFractionDigits(2);
		decimalFormatter.setMaximumFractionDigits(2);
	}

	public static boolean isAllowedName(String name) {
		try {
			return Config.nameRegex.trim().isEmpty() || Pattern.matches(Config.nameRegex, name);
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

	public static String format(long i) {
		return integerFormatter.format(i);
	}

	public static String format(Double d) {
		return format(BigDecimal.valueOf(d));
	}

	public static String format(BigDecimal bd) {
		return decimalFormatter.format(bd.setScale(2, RoundingMode.HALF_EVEN));
	}

	public static Location blockifyLocation(Location loc) {
		return new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
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
		return block.getType() == Material.CHEST
				|| block.getType() == Material.TRAPPED_CHEST
				|| block.getBlockData() instanceof Slab
				|| block.getBlockData() instanceof Stairs
				|| block.getType().isTransparent();
	}

	public static BukkitRunnable bukkitRunnable(Runnable runnable) {
		return new BukkitRunnable() {
			@Override
			public void run() {
				runnable.run();
			}
		};
	}

	public static <T> T nonNull(T ifNotNull, Supplier<T> ifNull) {
		return ternary(ifNotNull, ifNull, Objects::nonNull);
	}

	public static <T> T ternary(T ifTrue, Supplier<T> ifFalse, Predicate<? super T> pred) {
		return pred.test(ifTrue) ? ifTrue : ifFalse.get();
	}

	public static void depositPlayer(OfflinePlayer recipient, String worldName, double amount, Callback<Void> callback) {
		if (recipient == null)
			return;
		if (amount <= 0)
			return;

		EconomyResponse response = BankingPlugin.getInstance().getEconomy().depositPlayer(recipient, worldName, amount);
		if (response.transactionSuccess()) {
			callback.callSyncResult(null);
			return;
		}
		callback.callSyncError(new TransactionFailedException(response.errorMessage));
	}

	public static boolean withdrawPlayer(OfflinePlayer payer, String worldName, double amount, Callback<Void> callback) {
		if (payer == null)
			return false;
		if (amount <= 0)
			return true;

		EconomyResponse response = BankingPlugin.getInstance().getEconomy().withdrawPlayer(payer, worldName, amount);
		if (response.transactionSuccess()) {
			callback.callSyncResult(null);
			return true;
		}
		callback.callSyncError(new TransactionFailedException(response.errorMessage));
		return false;
	}

	public static void notifyPlayers(String message, OfflinePlayer player, Essentials essentials) {
		if (player == null)
			return;
		if (player.isOnline())
			message(player, message);
		else
			mail(player, message, essentials);
	}

	public static void notifyPlayers(String message, OfflinePlayer player) {
		notifyPlayers(message, player, BankingPlugin.getInstance().getEssentials());
	}

	public static void notifyPlayers(String message, Collection<OfflinePlayer> players) {
		Essentials essentials = BankingPlugin.getInstance().getEssentials();
		players.forEach(p -> notifyPlayers(message, p, essentials));
	}

	public static void message(Collection<OfflinePlayer> players, String message) {
		if (players == null)
			return;
		players.forEach(p -> message(p, message));
	}

	public static void message(OfflinePlayer player, String message) {
		if (player == null || !player.isOnline())
			return;
		player.getPlayer().sendMessage(message);
	}

	public static void mail(OfflinePlayer player, String message, Essentials essentials) {
		if (!Config.enableMail || player == null)
			return;
		User user = essentials.getUserMap().getUser(player.getUniqueId());
		if (user != null)
			user.addMail(message);
	}

	public static void mail(Collection<OfflinePlayer> players, String message, Essentials essentials) {
		if (!Config.enableMail || players == null)
			return;
		players.forEach(p -> mail(p, message, essentials));
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
			if (list.get(i).equals(stackedList.get(level).get(0)))
				stackedList.get(level).add(list.get(i));
			else {
				stackedList.add(new ArrayList<>());
				stackedList.get(++level).add(list.get(i));
			}
		}
		return stackedList;
	}

	/**
	 * Get a set of locations of the inventory
	 * @param ih The inventory holder to get the locations of
	 * @return A set of 1 or 2 locations
	 */
    public static Set<Location> getChestLocations(InventoryHolder ih) {
		Set<Location> chestLocations = new HashSet<>();
		if (ih instanceof DoubleChest) {
			DoubleChest dc = (DoubleChest) ih;
			chestLocations.add(((Chest) dc.getLeftSide()).getLocation());
			chestLocations.add(((Chest) dc.getRightSide()).getLocation());
		} else
			chestLocations.add(ih.getInventory().getLocation());
		return chestLocations;
	}

	public static boolean samePlayer(OfflinePlayer p1, OfflinePlayer p2) {
		if (p1 == null || p2 == null)
			return false;
		return p1.getUniqueId().equals(p2.getUniqueId());
	}

	@SuppressWarnings("deprecation")
	public static OfflinePlayer getPlayer(String name) {
    	OfflinePlayer player = Bukkit.getPlayerExact(name);
    	if (player == null)
    		player = Bukkit.getOfflinePlayer(name);
    	return player.hasPlayedBefore() ? player : null;
	}

	public static List<String> getOnlinePlayerNames(BankingPlugin plugin) {
		return plugin.getServer().getOnlinePlayers().stream()
				.map(HumanEntity::getName)
				.sorted()
				.collect(Collectors.toList());
	}

	static long getLimit(Player player, String permission, long defaultLimit) {
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

	@SafeVarargs
	public static <T> Set<T> mergeCollections(Collection<? extends T>... collections) {
		return Arrays.stream(collections).flatMap(Collection::stream).collect(Collectors.toCollection(HashSet::new));
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
			collection = Collections.emptySet();
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
			collection = Collections.emptySet();
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
