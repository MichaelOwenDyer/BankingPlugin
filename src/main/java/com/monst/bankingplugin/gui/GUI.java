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
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An inventory-based GUI.
 * @param <T> the subject matter of this GUI
 */
public abstract class GUI<T> {

	enum GUIType {
		BANK, BANK_LIST, ACCOUNT, ACCOUNT_LIST, ACCOUNT_CONTENTS, ACCOUNT_SHULKER_CONTENTS, ACCOUNT_RECOVERY,
        ACCOUNT_TRANSACTION_LOG, ACCOUNT_INTEREST_LOG, BANK_INCOME_LOG
	}

	static final List<String> NO_PERMISSION = Collections.singletonList("You do not have permission to view this.");

	GUI<?> parentGUI;
	boolean inForeground = true;
	boolean needsUpdate = false;

	final Menu.CloseHandler CLOSE_HANDLER = (player, menu) ->
			Utils.bukkitRunnable(() -> { // TODO: Can remove BukkitRunnable?
				if (isInForeground() && hasParent()) {
					parentGUI.inForeground = true;
					parentGUI.reopen(player);
					unsubscribe();
				}
			}).runTask(BankingPlugin.getInstance());

	/**
	 * Gets the {@link Observable} this GUI is observing for updates, or {@code null}
	 * if this GUI will not be automatically updated.
	 * @return the subject of this GUI, or null
	 */
	abstract Observable getSubject();

	void subscribe() {
		Observable observable = getSubject();
		if (observable != null)
			observable.addObserver(this);
	}

	void unsubscribe() {
		Observable observable = getSubject();
		if (observable != null)
			observable.removeObserver(this);
	}

	/**
	 * Opens this GUI for the specified player, shortening the GUI chain.
	 * This should be used when opening a new GUI.
	 */
	public abstract void open(Player player);

	/**
	 * Opens this GUI for the specified player without shortening the GUI chain.
	 * This should be used when returning to a previous GUI in the chain.
	 */
	abstract void reopen(Player player);

	/**
	 * Reloads the information in this GUI.
	 * This will only affect GUIs already open in the foreground; background GUIs will not be updated.
	 */
	public abstract void update();

	/**
	 * Closes the GUI for the player, opening the parent GUI if it exists.
	 * @param player the player to close this GUI for
	 */
	abstract void close(Player player);

	/**
	 * Exits the GUI for the player, ignoring the parent GUI.
	 * @param player the player to exit this GUI for
	 */
	void exit(Player player) {
		parentGUI = null;
		close(player);
	}

	abstract GUIType getType();

	/**
	 * Sets the parent, or the "source GUI", of this GUI.
	 * The parent will be reopened after this GUI is closed.
	 * @param parentGUI the parent GUI
	 * @return this GUI, to be opened
	 */
	public GUI<T> setParentGUI(@Nullable GUI<?> parentGUI) {
		if (parentGUI != null)
			parentGUI.inForeground = false;
		this.parentGUI = parentGUI;
		return this;
	}

	/**
	 * Descends down the list of previous open GUIs, and severs the link when it
	 * finds a GUI of a type it has seen before. This prevents the GUI chain
	 * from becoming too long and unwieldy.
	 */
	void shortenGUIChain() {
		shortenGUIChain(parentGUI, EnumSet.of(getType()));
	}

	private void shortenGUIChain(GUI<?> gui, EnumSet<GUIType> types) {
		if (gui == null)
			return;
		if (types.add(gui.getType()))
			shortenGUIChain(gui.parentGUI, types);
		else
			gui.parentGUI = null;
	}

	/**
	 * Returns whether or not this GUI is in the foreground (as opposed to hidden by another GUI)
	 * @return true if this GUI is in the foreground
	 */
	boolean isInForeground() {
		return inForeground;
	}

	/**
	 * Returns whether or not this GUI has a parent GUI.
	 * @return true if this GUI has a parent
	 */
	boolean hasParent() {
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

	static List<String> wordWrapAll(Stream<String> lore) {
		return wordWrapAll(30, lore);
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
