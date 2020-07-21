package com.monst.bankingplugin.gui;

import com.sun.istack.internal.NotNull;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.Slot.ClickHandler;

import java.util.List;
import java.util.stream.Collectors;

abstract class Gui<Ownable> {

	Ownable guiSubject;
	Menu gui;
	boolean verbose;

	static final Material GENERAL_INFO_BLOCK = Material.PLAYER_HEAD;
	static final Material MULTIPLIER_INFO_BLOCK = Material.GOLD_BLOCK;

	public void open(Player player) {
		gui = getMenu();
		verbose = getClearance(player);
		for (int i = 0; i < gui.getDimensions().getArea(); i++) {
			gui.getSlot(i).setItem(createSlotItem(i));
			gui.getSlot(i).setClickHandler(createClickHandler(i));
		}
		gui.open(player);
	}

	abstract Menu getMenu();

	abstract boolean getClearance(Player player);

	abstract ItemStack createSlotItem(int i);

	abstract ClickHandler createClickHandler(int i);

	static ItemStack createSlotItem(@NotNull Material material, @NotNull String displayName, @NotNull List<String> lore) {
		ItemStack item = new ItemStack(material);
		ItemMeta itemMeta = item.getItemMeta();
		assert itemMeta != null;
		itemMeta.setDisplayName(ChatColor.GRAY + displayName);
		itemMeta.setLore(lore.stream().map(s -> ChatColor.GRAY + s).collect(Collectors.toList()));
		item.setItemMeta(itemMeta);
		return item;
	}

}