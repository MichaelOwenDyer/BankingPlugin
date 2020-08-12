package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.utils.Ownable;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.Slot.ClickHandler;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

abstract class Gui<T extends Ownable> {

	static final BankingPlugin plugin = BankingPlugin.getInstance();
	final T guiSubject;
	Menu menu;
	Gui<T> prevGui;
	boolean openInBackground = false;

	static final List<String> NO_PERMISSION = Collections.singletonList("You do not have permission to view this.");
	final Menu.CloseHandler OPEN_PREVIOUS = (player, menu) -> new BukkitRunnable() {
		@Override
		public void run() {
			if (isLinked() && !isOpenInBackground()) {
				prevGui.openInBackground = false;
				prevGui.open(player, false);
			}
		}
	}.runTaskLater(plugin, 0);

	Gui(T guiSubject) {
		this.guiSubject = guiSubject;
	}

	public void open(Player player) {
		open(player, true);
	}

	void open(Player player, boolean update) {
		if (update) {
			createMenu();
			setCloseHandler(OPEN_PREVIOUS);
			shortenGuiChain();
		}
		evaluateClearance(player);
		for (int i = 0; i < menu.getDimensions().getArea(); i++) {
			menu.getSlot(i).setItem(createSlotItem(i));
			menu.getSlot(i).setClickHandler(createClickHandler(i));
		}
		menu.open(player);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public Gui<T> setPrevGui(@Nullable Gui prevGui) {
		if (prevGui != null)
			prevGui.openInBackground = true;
		this.prevGui = prevGui;
		return this;
	}

	abstract void createMenu();

	abstract void evaluateClearance(Player player);

	abstract ItemStack createSlotItem(int i);

	abstract ClickHandler createClickHandler(int i);

	abstract void setCloseHandler(Menu.CloseHandler handler);

	abstract GuiType getType();

	static ItemStack createSlotItem(Material material, String displayName, List<String> lore) {
		return createSlotItem(new ItemStack(material), displayName, lore);
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
	 * @param item the {@link ItemStack} that should be used for the item
	 * @param displayName the name of the Gui item
	 * @param lore the description of the Gui item
	 * @return a custom {@link ItemStack}
	 */
	private static ItemStack createSlotItem(ItemStack item, String displayName, List<String> lore) {
		ItemMeta itemMeta = item.getItemMeta();
		if (itemMeta == null)
			return null;
		itemMeta.setDisplayName("" + ChatColor.GRAY + ChatColor.ITALIC + displayName);
		itemMeta.setLore(lore.stream().map(s -> ChatColor.GRAY + s).collect(Collectors.toList()));
		item.setItemMeta(itemMeta);
		return item;
	}

	/**
	 * Descends down the list of previous open Guis, and severs the link when it
	 * finds a Gui of a type it has seen before. This prevents the Gui chain
	 * from becoming too long and unwieldy.
	 */
	void shortenGuiChain() {
		shortenGuiChain(this, EnumSet.noneOf(GuiType.class));
	}

	private void shortenGuiChain(Gui<T> gui, EnumSet<GuiType> types) {
		Gui<T> previous = gui.prevGui;
		if (previous == null)
			return;
		if (!types.contains(previous.getType())) {
			types.add(previous.getType());
			shortenGuiChain(previous, types);
		} else {
			gui.prevGui = null;
		}
	}

	boolean isOpenInBackground() {
		return openInBackground;
	}

	boolean isLinked() {
		return prevGui != null;
	}

	enum GuiType {
		ACCOUNT, ACCOUNT_LIST, ACCOUNT_CONTENTS, BANK
	}
}
