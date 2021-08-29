package com.monst.bankingplugin.events.account;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * This event is fired when a player uses the
 * {@link com.monst.bankingplugin.commands.account.AccountTransfer} command.
 */
public class AccountTransferCommandEvent extends AccountCommandEvent implements Cancellable {

	private boolean cancelled;

	public AccountTransferCommandEvent(Player player, String[] args) {
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
