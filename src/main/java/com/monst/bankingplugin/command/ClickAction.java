package com.monst.bankingplugin.command;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.event.EventCancelledException;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class ClickAction {

	@FunctionalInterface
	public interface ChestAction {
		void onClick(Block chest) throws CommandExecutionException, EventCancelledException;
	}

	@FunctionalInterface
	public interface AccountAction {
		void onClick(Account account) throws CommandExecutionException, EventCancelledException;
	}

	private static final Cache<UUID, ClickAction> PLAYER_CLICK_ACTIONS = CacheBuilder.newBuilder()
			.expireAfterWrite(15, TimeUnit.SECONDS)
			.build();

	public static void setBlockClickAction(Player player, ChestAction action) {
		PLAYER_CLICK_ACTIONS.asMap().put(player.getUniqueId(), new ClickAction(true) {
			@Override
			public void onClick(Block block) throws CommandExecutionException, EventCancelledException {
				action.onClick(block);
			}
		});
	}

	public static void setAccountClickAction(Player player, AccountAction action) {
		PLAYER_CLICK_ACTIONS.asMap().put(player.getUniqueId(), new ClickAction(false) {
			@Override
			public void onClick(Account account) throws CommandExecutionException, EventCancelledException {
				action.onClick(account);
			}
		});
	}

	private final boolean isBlockAction;
	private boolean confirmed = false;

	private ClickAction(boolean isBlockAction) {
		this.isBlockAction = isBlockAction;
	}

	public boolean isBlockAction() {
		return isBlockAction;
	}

	public void onClick(Account account) throws CommandExecutionException, EventCancelledException {
		// Do nothing
	}

	public void onClick(Block block) throws CommandExecutionException, EventCancelledException {
		// Do nothing
	}

	/**
	 * Gets the click action of a player
	 * @param player Player whose click action to get
	 * @return The player's click action or <b>null</b> if none
	 */
	public static ClickAction get(Player player) {
		return PLAYER_CLICK_ACTIONS.asMap().get(player.getUniqueId());
	}

	/**
	 * Removes the click action from a player.
	 * @param player Player to remove the click action from
	 */
	public static void remove(Player player) {
		PLAYER_CLICK_ACTIONS.asMap().remove(player.getUniqueId());
	}

	public static boolean mustConfirm(Player player) {
		ClickAction clickAction = get(player);
		if (clickAction == null)
			return true;
		if (clickAction.confirmed)
			return false;
		clickAction.confirmed = true;
		PLAYER_CLICK_ACTIONS.put(player.getUniqueId(), clickAction);
		return true;
	}

}
