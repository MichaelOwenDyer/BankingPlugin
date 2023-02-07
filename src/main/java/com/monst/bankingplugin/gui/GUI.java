package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.ChatPaginator;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An inventory-based GUI.
 */
public abstract class GUI implements InventoryHolder {
	
	static final List<String> NO_PERMISSION = Collections.singletonList("You do not have permission to view this.");
	
	final BankingPlugin plugin;
	final Player player;
	Inventory inventory;
	
	GUI parentGUI;
	boolean visible = true;
	
	public GUI(BankingPlugin plugin, Player player) {
		this.plugin = plugin;
		this.player = player;
	}
	
	/**
	 * Opens this GUI for the specified player, shortening the GUI chain.
	 * This should be used when opening a new GUI.
	 */
	public abstract void open();
	
	abstract Inventory createInventory();
	
	@Override
	public Inventory getInventory() {
		return inventory;
	}

	/**
	 * Opens this GUI for the specified player without shortening the GUI chain.
	 * This should be used when returning to a previous GUI in the chain.
	 */
	void reopen() {
		visible = true;
		player.openInventory(inventory);
	}
	
	public void click(int slot, ClickType type) {}
	
	/**
	 * Exits the GUI for the player, ignoring the parent GUI.
	 */
	void exit() {
		parentGUI = null;
		player.closeInventory();
	}

	/**
	 * Upon closing of this GUI, open the parent GUI if it exists.
	 */
	public void onClose() {
		// FIXME: Could this have something to do with the flickering on close?
		if (visible && parentGUI != null)
			Bukkit.getScheduler().runTask(plugin, parentGUI::reopen);
	}
	
	/**
	 * Marks the provided GUI as a child of this GUI.
	 * This GUI will no longer be in the foreground when the child GUI is open.
	 */
	GUI child(GUI child) {
		visible = false;
		child.parentGUI = this;
		return child;
	}

	/**
	 * Descends the list of previous open GUIs, and severs the link when it
	 * finds a GUI of a type it has seen before. This prevents the GUI chain
	 * from becoming too long and unwieldy.
	 */
	void shortenGUIChain() {
		shortenGUIChain(parentGUI, new HashSet<>(Collections.singleton(getClass())));
	}

	private void shortenGUIChain(GUI gui, Set<Class<?>> types) {
		if (gui == null)
			return;
		if (types.add(gui.getClass()))
			shortenGUIChain(gui.parentGUI, types);
		else
			gui.parentGUI = null;
	}

	/**
	 * Create a specialized player head {@link ItemStack} to be placed in the GUI.
	 * @param owner the {@link OfflinePlayer} whose head should be used
	 * @param displayName the name of the GUI item
	 * @param lore the description of the GUI item
	 * @return a custom player head {@link ItemStack}
	 */
	static ItemStack head(OfflinePlayer owner, String displayName, List<String> lore) {
		ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
		if (owner != null) {
			SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
			skullMeta.setOwningPlayer(owner);
			skull.setItemMeta(skullMeta);
		}
		return setTitleAndLore(skull, displayName, lore);
	}
	
	static ItemStack head(OfflinePlayer owner, String displayName, String... lore) {
		return head(owner, displayName, Arrays.asList(lore));
	}

	/**
	 * Create an {@link ItemStack} to be placed in the GUI.
	 * @param material the {@link Material} that should be used for the item
	 * @param title the name of the GUI item
	 * @param lore the description of the GUI item
	 * @return a custom {@link ItemStack}
	 */
	static ItemStack item(Material material, String title, List<String> lore) {
		return setTitleAndLore(new ItemStack(material), title, lore);
	}
	
	static ItemStack item(Material material, String title, String... lore) {
		return setTitleAndLore(new ItemStack(material), title, Arrays.asList(lore));
	}

	private static ItemStack setTitleAndLore(ItemStack item, String displayName, List<String> lore) {
		ItemMeta itemMeta = item.getItemMeta();
		itemMeta.setDisplayName("" + ChatColor.GRAY + ChatColor.ITALIC + displayName);
		itemMeta.setLore(lore.stream().map(s -> ChatColor.GRAY + s).collect(Collectors.toList()));
		item.setItemMeta(itemMeta);
		return item;
	}

	static List<String> wordWrapAll(Stream<String> lore) {
		return wordWrapAll(30, lore);
	}

	static List<String> wordWrapAll(String... args) {
		return wordWrapAll(30, Stream.of(args));
	}

	static List<String> wordWrapAll(int lineLength, Stream<String> lines) {
		return lines.map(s -> ChatPaginator.wordWrap(s, lineLength))
				.flatMap(Stream::of)
				.map(s -> s.replace("" + ChatColor.WHITE, ""))
				.collect(Collectors.toList());
	}

	static void teleport(Player player, Location location) {
		player.teleport(location.setDirection(player.getLocation().getDirection()));
	}

	String format(BigDecimal bd) {
		return plugin.getEconomy().format(bd.doubleValue());
	}

	String formatAndColorize(BigDecimal bd) {
		if (bd.signum() > 0)
			return ChatColor.GREEN + format(bd);
		if (bd.signum() < 0)
			return ChatColor.RED + format(bd);
		return ChatColor.GRAY + format(bd);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GUI gui = (GUI) o;
		return visible == gui.visible && player.equals(gui.player);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(player, visible);
	}
}
