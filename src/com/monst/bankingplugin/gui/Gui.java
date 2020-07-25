package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.utils.Ownable;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.Slot.ClickHandler;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

abstract class Gui<T extends Ownable> {

	BankingPlugin plugin;
	T guiSubject;
	Menu menu;
	Gui<T> prevGui;
	boolean openInBackground = false;

	final Menu.CloseHandler CLOSE_HANDLER = (player, menu) -> new BukkitRunnable() {
		@Override
		public void run() {
			if (prevGui != null && !openInBackground) {
				prevGui.openInBackground = false;
				prevGui.open(player, false);
			}
		}
	}.runTaskLater(plugin, 0);
	static final Material GENERAL_INFO_BLOCK = Material.PLAYER_HEAD;
	static final Material MULTIPLIER_INFO_BLOCK = Material.NETHER_STAR;

	Gui(BankingPlugin plugin, T t) {
		this.plugin = plugin;
		this.guiSubject = t;
	}

	public void open(Player player) {
		open(player, true);
	}

	void open(Player player, boolean update) {
		if (update) {
			menu = getMenu();
			setCloseHandler(CLOSE_HANDLER);
			shortenGuiChain(prevGui);
		}
		evaluateClearance(player);
		for (int i = 0; i < menu.getDimensions().getArea(); i++) {
			menu.getSlot(i).setItem(createSlotItem(i));
			menu.getSlot(i).setClickHandler(createClickHandler(i));
		}
		menu.open(player);
	}

	@SuppressWarnings("rawtypes")
	public Gui<T> setPrevGui(@Nullable Gui prevGui) {
		if (prevGui != null)
			prevGui.openInBackground = true;
		this.prevGui = prevGui;
		return this;
	}

	abstract Menu getMenu();

	abstract void evaluateClearance(Player player);

	abstract ItemStack createSlotItem(int i);

	abstract ClickHandler createClickHandler(int i);

	abstract void setCloseHandler(Menu.CloseHandler handler);

	abstract GuiType getType();

	static ItemStack createSlotItem(Material material, String displayName, List<String> lore) {
		return createSlotItem(new ItemStack(material), displayName, lore);
	}

	static ItemStack createSlotItem(OfflinePlayer owner, String displayName, List<String> lore) {
		ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
		if (skullMeta != null)
			skullMeta.setOwningPlayer(owner);
		skull.setItemMeta(skullMeta);
		return createSlotItem(skull, displayName, lore);
	}

	private static ItemStack createSlotItem(ItemStack item, String displayName, List<String> lore) {
		ItemMeta itemMeta = item.getItemMeta();
		if (itemMeta == null)
			return null;
		itemMeta.setDisplayName(ChatColor.GRAY + displayName);
		itemMeta.setLore(lore.stream().map(s -> ChatColor.GRAY + s).collect(Collectors.toList()));
		item.setItemMeta(itemMeta);
		return item;
	}

	/**
	 * Descends down the list of previous open menus, and severs the link when it
	 * finds a certain number of the same type as the current gui. This prevents the gui chain
	 * from becoming uncontrollably long.
	 * @param gui The gui to compare to the current one
	 */
	private void shortenGuiChain(Gui<T> gui) {
		shortenGuiChain(gui, EnumSet.noneOf(GuiType.class));
	}

	private void shortenGuiChain(Gui<T> gui, EnumSet<GuiType> types) {
		if (gui == null)
			return;
		if (!types.contains(gui.getType())) {
			types.add(gui.getType());
			shortenGuiChain(gui.prevGui, types);
		} else {
			gui.prevGui = null;
			gui.setCloseHandler(null); // TODO: Would just one of these be sufficient?
		}
	}

	enum GuiType {
		ACCOUNT, ACCOUNT_LIST, ACCOUNT_CONTENTS, BANK
	}
}
