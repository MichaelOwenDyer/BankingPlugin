package com.monst.bankingplugin.gui;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.account.Account;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.banking.bank.BankField;
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

public abstract class Gui<T> {

	enum GuiType {
		BANK, BANK_LIST, ACCOUNT, ACCOUNT_LIST, ACCOUNT_CONTENTS, ACCOUNT_SHULKER_CONTENTS, ACCOUNT_RECOVERY
	}

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

	static List<String> getMultiplierLore(Bank bank) {
		return getMultiplierLore(bank.get(BankField.MULTIPLIERS), -1);
	}

	static List<String> getMultiplierLore(Account account) {
		return getMultiplierLore(account.getBank().get(BankField.MULTIPLIERS), account.getStatus().getMultiplierStage());
	}

	private static List<String> getMultiplierLore(List<Integer> multipliers, int highlightStage) {

		if (multipliers.isEmpty())
			return Collections.singletonList(ChatColor.GREEN + "1x");

		List<List<Integer>> stackedMultipliers = Utils.stackList(multipliers);

		int stage = -1;
		if (highlightStage != -1)
			for (List<Integer> level : stackedMultipliers) {
				stage++;
				if (highlightStage - level.size() < 0)
					break;
				else
					highlightStage -= level.size();
			}

		List<String> lore = new ArrayList<>();

		final int listSize = 5;
		int lower = 0;
		int upper = stackedMultipliers.size();

		if (stage != -1 && stackedMultipliers.size() > listSize) {
			lower = stage - (listSize / 2);
			upper = stage + (listSize / 2) + 1;
			while (lower < 0) {
				lower++;
				upper++;
			}
			while (upper > stackedMultipliers.size()) {
				lower--;
				upper--;
			}

			if (lower > 0)
				lore.add("...");
		}

		for (int i = lower; i < upper; i++) {
			StringBuilder number = new StringBuilder("" + ChatColor.GOLD + (i == stage ? ChatColor.BOLD : ""));

			number.append(" - ").append(stackedMultipliers.get(i).get(0)).append("x" + ChatColor.DARK_GRAY);

			int levelSize = stackedMultipliers.get(i).size();
			if (levelSize > 1) {
				if (stage == -1) {
					number.append(" (" + ChatColor.GRAY + "x" + ChatColor.AQUA + levelSize + ChatColor.DARK_GRAY + ")");
				} else if (i < stage) {
					number.append(" (" + ChatColor.GREEN + levelSize + ChatColor.DARK_GRAY + "/" + ChatColor.GREEN + levelSize + ChatColor.DARK_GRAY + ")");
				} else if (i > stage) {
					number.append(" (" + ChatColor.RED + "0" + ChatColor.DARK_GRAY + "/" + ChatColor.GREEN + levelSize + ChatColor.DARK_GRAY + ")");
				} else {
					ChatColor color;
					if (highlightStage * 3 >= levelSize * 2)
						color = ChatColor.GREEN;
					else if (highlightStage * 3 >= levelSize)
						color = ChatColor.GOLD;
					else
						color = ChatColor.RED;
					number.append(" (" + color + highlightStage + ChatColor.DARK_GRAY + "/" + ChatColor.GREEN + levelSize + ChatColor.DARK_GRAY + ")");
				}
			}
			lore.add(number.toString());
		}
		if (upper < stackedMultipliers.size())
			lore.add("...");
		return lore;
	}
}
