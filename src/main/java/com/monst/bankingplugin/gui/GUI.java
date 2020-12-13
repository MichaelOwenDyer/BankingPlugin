package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.utils.Observable;
import com.monst.bankingplugin.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.ChatPaginator;
import org.ipvp.canvas.Menu;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class GUI<T> {

	enum GUIType {
		BANK, BANK_LIST, ACCOUNT, ACCOUNT_LIST, ACCOUNT_CONTENTS, ACCOUNT_SHULKER_CONTENTS, ACCOUNT_RECOVERY
	}

	static BankingPlugin plugin = BankingPlugin.getInstance();

	GUI<?> parentGUI;
	Player viewer;
	boolean inForeground = true;

	static final List<String> NO_PERMISSION = Collections.singletonList("You do not have permission to view this.");
	final Menu.CloseHandler OPEN_PARENT = (player, menu) -> Utils.bukkitRunnable(() -> {
		if (isLinked() && isInForeground()) {
			parentGUI.inForeground = true;
			parentGUI.open(false);
		}
	}).runTask(plugin);

	public void open(Player player) {
		this.viewer = player;
		open(true);
	}

	void subscribe(Observable observable) {
		observable.addObserver(this);
	}

	void unsubscribe(Observable observable) {
		observable.removeObserver(this);
	}

	abstract void open(boolean initialize);

	public abstract void update();

	abstract void close(Player player);

	abstract void initializeMenu();

	abstract void setCloseHandler(Menu.CloseHandler handler);

	abstract GUIType getType();

	public GUI<T> setParentGUI(@Nullable GUI<?> parentGUI) {
		if (parentGUI != null)
			parentGUI.inForeground = false;
		this.parentGUI = parentGUI;
		return this;
	}

	void shortenGUIChain() {
		shortenGUIChain(parentGUI, EnumSet.of(getType()));
	}

	/**
	 * Descends down the list of previous open GUIs, and severs the link when it
	 * finds a GUI of a type it has seen before. This prevents the GUI chain
	 * from becoming too long and unwieldy.
	 */
	private void shortenGUIChain(GUI<?> gui, EnumSet<GUIType> types) {
		if (gui == null)
			return;
		if (!types.contains(gui.getType())) {
			types.add(gui.getType());
			shortenGUIChain(gui.parentGUI, types);
		} else
			gui.parentGUI = null;
	}

	boolean isInForeground() {
		return inForeground;
	}

	boolean isLinked() {
		return parentGUI != null;
	}

	/**
	 * Create a specialized player head {@link ItemStack} to be placed in the GUI.
	 * @param owner the {@link OfflinePlayer} whose head should be used
	 * @param displayName the name of the GUI item
	 * @param lore the description of the GUI item
	 * @return a custom player head {@link ItemStack}
	 */
	static ItemStack createSlotItem(OfflinePlayer owner, String displayName, List<String> lore) {
		ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
		if (skullMeta != null)
			skullMeta.setOwningPlayer(owner);
		skull.setItemMeta(skullMeta);
		return createSlotItem(skull, displayName, lore);
	}

	/**
	 * Create an {@link ItemStack} to be placed in the GUI.
	 * @param material the {@link Material} that should be used for the item
	 * @param displayName the name of the GUI item
	 * @param lore the description of the GUI item
	 * @return a custom {@link ItemStack}
	 */
	static ItemStack createSlotItem(Material material, String displayName, List<String> lore) {
		return createSlotItem(new ItemStack(material), displayName, lore);
	}

	private static ItemStack createSlotItem(ItemStack item, String displayName, List<String> lore) {
		ItemMeta itemMeta = item.getItemMeta();
		if (itemMeta == null)
			return null;
		itemMeta.setDisplayName("" + ChatColor.GRAY + ChatColor.ITALIC + displayName);
		itemMeta.setLore(Utils.map(lore, s -> ChatColor.GRAY + s));
		item.setItemMeta(itemMeta);
		return item;
	}

	static List<String> wordWrapAll(List<String> lore) {
		return wordWrapAll(30, lore.stream());
	}

	static List<String> wordWrapAll(int lineLength, List<String> lore) {
		return wordWrapAll(lineLength, lore.stream());
	}

	static List<String> wordWrapAll(String... args) {
		return wordWrapAll(30, Arrays.stream(args));
	}

	static List<String> wordWrapAll(int lineLength, String... args) {
		return wordWrapAll(lineLength, Arrays.stream(args));
	}

	static List<String> wordWrapAll(int lineLength, Stream<String> lines) {
		return lines.map(s -> ChatPaginator.wordWrap(s, lineLength))
				.flatMap(Arrays::stream)
				.map(s -> s.replace("" + ChatColor.WHITE, ""))
				.collect(Collectors.toList());
	}
}
