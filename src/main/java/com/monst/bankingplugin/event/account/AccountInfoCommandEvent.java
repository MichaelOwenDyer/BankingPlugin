package com.monst.bankingplugin.event.account;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * This event is fired when a player uses the {@link com.monst.bankingplugin.command.account.AccountInfo}
 * command without specifying an account ID.
 */
public class AccountInfoCommandEvent extends AccountCommandEvent implements Cancellable {

	private boolean cancelled;

	public AccountInfoCommandEvent(Player player, String[] args) {
		super(player, args);
	}

	private static final HandlerList HANDLERS = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}

}
