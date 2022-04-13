package com.monst.bankingplugin.event.account;

import com.monst.bankingplugin.entity.Account;
import com.monst.bankingplugin.command.ClickAction;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * This event is fired when a {@link Player} and a {@link ClickAction} of type <b>create</b>
 * clicks on a chest and creates an account.
 */
public class AccountOpenEvent extends SingleAccountEvent implements Cancellable {

	private boolean cancelled;

	public AccountOpenEvent(Player player, Account account) {
		super(player, account);
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
