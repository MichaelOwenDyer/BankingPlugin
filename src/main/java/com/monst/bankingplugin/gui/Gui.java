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
import org.ipvp.canvas.Menu;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public abstract class Gui<T> {

	static BankingPlugin plugin = BankingPlugin.getInstance();

	Gui<?> prevGui;
	Player viewer;
	boolean inForeground = true;

	static final List<String> NO_PERMISSION = Collections.singletonList("You do not have permission to view this.");
	final Menu.CloseHandler OPEN_PREVIOUS = (player, menu) -> Utils.bukkitRunnable(() -> {
		if (isLinked() && isInForeground()) {
			prevGui.inForeground = true;
			prevGui.open(false);
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

	abstract GuiType getType();

	public Gui<T> setPrevGui(@Nullable Gui<?> prevGui) {
		if (prevGui != null)
			prevGui.inForeground = false;
		this.prevGui = prevGui;
		return this;
	}

	/**
	 * Create a specialized player head {@link ItemStack} to be placed in the Gui.
	 * @param owner the {@link OfflinePlayer} whose head should be used
	 * @param displayName the name of the Gui item
	 * @param lore the description of the Gui item
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
	 * Create an {@link ItemStack} to be placed in the Gui.
	 * @param material the {@link Material} that should be used for the item
	 * @param displayName the name of the Gui item
	 * @param lore the description of the Gui item
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

	void shortenGuiChain() {
		shortenGuiChain(prevGui, EnumSet.of(getType()));
	}

	/**
	 * Descends down the list of previous open Guis, and severs the link when it
	 * finds a Gui of a type it has seen before. This prevents the Gui chain
	 * from becoming too long and unwieldy.
	 */
	private void shortenGuiChain(Gui<?> gui, EnumSet<GuiType> types) {
		if (gui == null)
			return;
		if (!types.contains(gui.getType())) {
			types.add(gui.getType());
			shortenGuiChain(gui.prevGui, types);
		} else
			gui.prevGui = null;
	}

	boolean isInForeground() {
		return inForeground;
	}

	boolean isLinked() {
		return prevGui != null;
	}

	enum GuiType {
		BANK, BANK_LIST, ACCOUNT, ACCOUNT_LIST, ACCOUNT_CONTENTS, ACCOUNT_SHULKER_CONTENTS, ACCOUNT_RECOVERY
	}
}
