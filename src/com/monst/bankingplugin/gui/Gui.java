package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.utils.Ownable;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.Slot.ClickHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

abstract class Gui<T extends Ownable> {

	BankingPlugin plugin;
	T guiSubject;
	Menu menu;
	Gui<T> prevMenu;
	boolean openInBackground = false;

	static final Material GENERAL_INFO_BLOCK = Material.PLAYER_HEAD;
	static final Material MULTIPLIER_INFO_BLOCK = Material.NETHER_STAR;

	Gui(BankingPlugin plugin, T t) {
		this.plugin = plugin;
		this.guiSubject = t;
	}

	public void open(Player player) {
		menu = getMenu();
		menu.setCloseHandler((player1, menu1) -> new BukkitRunnable() {
			@Override
			public void run() {
				if (prevMenu != null && !openInBackground) {
					prevMenu.openInBackground = false;
					prevMenu.open(player);
				}
			}
		}.runTaskLater(plugin, 0));
		evaluateClearance(player);
		for (int i = 0; i < menu.getDimensions().getArea(); i++) {
			menu.getSlot(i).setItem(createSlotItem(i));
			menu.getSlot(i).setClickHandler(createClickHandler(i));
		}
		shortenGuiChain(prevMenu, 0);
		menu.open(player);
	}

	public Gui<T> setPrevMenu(@Nullable Gui prevMenu) {
		if (prevMenu != null)
			prevMenu.openInBackground = true;
		this.prevMenu = prevMenu;
		return this;
	}

	abstract Menu getMenu();

	abstract void evaluateClearance(Player player);

	abstract ItemStack createSlotItem(int i);

	abstract ClickHandler createClickHandler(int i);

	abstract GuiType getType();

	static ItemStack createSlotItem(Material material, String displayName, List<String> lore) {
		ItemStack item = new ItemStack(material);
		ItemMeta itemMeta = item.getItemMeta();
		if (itemMeta == null)
			return null;
		itemMeta.setDisplayName(ChatColor.GRAY + displayName);
		itemMeta.setLore(lore.stream().map(s -> ChatColor.GRAY + s).collect(Collectors.toList()));
		item.setItemMeta(itemMeta);
		return item;
	}

	static ItemStack createSlotItem(OfflinePlayer owner, String displayName, List<String> lore) {
		return null; // TODO: Use for generating custom player heads in GUI
	}

	/**
	 * Descends down the list of previous open menus, and severs the link when it
	 * finds a certain number of the same type as the current menu. This prevents the menu chain
	 * from becoming uncontrollably long.
	 * @param menu The menu to compare to the current one
	 */
	private void shortenGuiChain(Gui<T> menu, int count) {
		if (menu == null)
			return;
		if (menu.getType() == this.getType())
			count++;
		if (count >= 2)
			menu = null;
		else
			shortenGuiChain(menu.prevMenu, count);
	}

	enum GuiType {
		ACCOUNT, ACCOUNT_LIST, ACCOUNT_CONTENTS, BANK
	}
}
